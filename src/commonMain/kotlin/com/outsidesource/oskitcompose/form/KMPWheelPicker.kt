package com.outsidesource.oskitcompose.form

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.lib.VarRef
import com.outsidesource.oskitcompose.modifier.kmpMouseScrollFilter
import com.outsidesource.oskitcompose.pointer.awaitFirstUp
import com.outsidesource.oskitkmp.concurrency.Debouncer
import com.outsidesource.oskitkmp.lib.Platform
import com.outsidesource.oskitkmp.lib.current
import kotlinx.coroutines.launch
import kotlin.math.abs


/**
 * [KMPWheelPicker] a cross-platform wheel picker.
 *
 * @param selectedIndex The index of the current value
 * @param items The list of items for the picker to display
 * @param itemKey A factory of stable and unique keys representing the item
 * @param state The state for the picker
 * @param modifier The Compose Modifier for KMPWheelPicker
 * @param enabled Enables or disables user interaction with the picker
 * @param onChange Callback for when the picker fully settles on a value
 * @param onImmediateChange Callback for any time a new value passes through the indication window. It is not recommended
 * to change the value with this callback. It can instead be used for reacting quickly to value changes
 * @param scrollEffect The effect applied to items as they scroll
 * @param indicator The indicator for the current selection
 * @param content The content for each item
 *
 * Note: All items must be the same height.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> KMPWheelPicker(
    selectedIndex: Int,
    items: List<T>,
    itemKey: (T, Int) -> Any = { _, index -> index },
    state: KMPWheelPickerState = rememberKmpWheelPickerState(isInfinite = false, initiallySelectedItemIndex = selectedIndex),
    modifier: Modifier = Modifier,
    enabled : Boolean = true,
    onChange: (T) -> Unit,
    onImmediateChange: (T) -> Unit = {},
    scrollEffect: KMPWheelPickerScrollEffect = remember { KMPWheelPickerScrollEffects.magnify() },
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    indicator: KMPWheelPickerIndicator = remember { KMPWheelPickerIndicators.window() },
    content: @Composable LazyItemScope.(T) -> Unit,
) {
    val internalSelectedIndex = remember(selectedIndex, state) { if (state.isInfinite) selectedIndex + INFINITE_OFFSET else selectedIndex }
    val isDragging = remember { VarRef(false) }
    val velocityTracker = remember { VelocityTracker() }
    val scope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(state.lazyListState)
    val scrollDebouncer = remember { Debouncer(timeoutMillis = 250, scope = scope) }

    val paddingValues = with(LocalDensity.current) {
        remember(state.verticalPadding) {
            PaddingValues(vertical = state.verticalPadding.toDp())
        }
    }

    val handleOnChange = remember(items, state) {
        handleOnChange@ { index: Int ->
            val oldItemIndex = getItemsIndex(state.lastOnChangeIndex, state, items.size)
            val newItemIndex = getItemsIndex(index, state, items.size)
            if (oldItemIndex == newItemIndex) return@handleOnChange

            state.lastOnChangeIndex = index
            onChange(items[newItemIndex])
        }
    }

    // This fixes an issue with non-infinite wheels set to the last index in the list not showing the selection properly due to padding not being calculated initially.
    LaunchedEffect(paddingValues) {
        state.scrollToItem(selectedIndex)
    }

    // Handle onImmediateChange callback
    LaunchedEffect(state, items) {
        state.selectedItemRawIndexFlow.collect {
            onImmediateChange(items[getItemsIndex(it, state, items.size)])
        }
    }

    // Handle value changing outside KMPWheelPicker
    LaunchedEffect(internalSelectedIndex, selectedIndex, items) {
        // If the new value equals the old value don't do anything
        if (isDragging.value || getItemsIndex(state.lastOnChangeIndex, state, items.size) == selectedIndex) return@LaunchedEffect
        state.lastOnChangeIndex = internalSelectedIndex
        state.animateScrollToItem(getItemsIndex(internalSelectedIndex, state, items.size), items.size)
    }

    LazyColumn(
        modifier = Modifier
            .drawWithContent { indicator(state) }
            .kmpMouseScrollFilter(state, handleOnChange) { _, _ ->
                if (!enabled) return@kmpMouseScrollFilter

                scrollDebouncer.emit {
                    val index = if (state.isInfinite) state.selectedItemRawIndex - INFINITE_OFFSET else state.selectedItemRawIndex
                    state.animateScrollToItem(index, items.size)
                    handleOnChange(state.selectedItemRawIndex)
                }
            }
            .pointerInput(state, handleOnChange) {
                if (!enabled) return@pointerInput

                awaitEachGesture {
                    // Cancel fling on down if actively flinging
                    awaitFirstDown(requireUnconsumed = false)
                    if (!state.lazyListState.isScrollInProgress) return@awaitEachGesture
                    scope.launch { state.lazyListState.scrollBy(0f) }

                    // Adjust to correct value
                    awaitFirstUp(requireUnconsumed = false)
                    scope.launch {
                        state.lazyListState.scroll {
                            with(flingBehavior) {
                                performFling(0f)
                                handleOnChange(state.selectedItemRawIndex)
                            }
                        }
                    }
                }
            }
            .pointerInput(state, handleOnChange) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        isDragging.value = true
                        velocityTracker.resetTracking()
                    },
                    onDrag = { change, delta ->
                        velocityTracker.addPointerInputChange(change)
                        state.lazyListState.dispatchRawDelta(-delta.y)
                    },
                    onDragEnd = {
                        isDragging.value = false
                        val velocity = -velocityTracker.calculateVelocity().y

                        scope.launch {
                            state.lazyListState.scroll {
                                with(flingBehavior) {
                                    performFling(velocity)
                                    handleOnChange(state.selectedItemRawIndex)
                                }
                            }
                        }
                    },
                )
            }
            .then(modifier),
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = Platform.current.isDesktop, // Allow wheel scroll if Desktop, otherwise the scrolling is handled via the pointerInput modifier
        state = state.lazyListState,
        contentPadding = paddingValues,
        flingBehavior = flingBehavior,
    ) {
        items(
            count = if (state.isInfinite) Int.MAX_VALUE else items.size,
            key = { itemKey(items[getItemsIndex(it, state, items.size)], it) }
        ) { index ->
            Box(
                modifier = Modifier
                    .pointerInput(state, handleOnChange) {
                        if (!enabled) return@pointerInput

                        detectTapGestures {
                            scope.launch {
                                state.lazyListState.animateScrollToItem(index)
                                handleOnChange(index)
                            }
                        }
                    }
                    .graphicsLayer {
                        val stepsFromSelected = index - state.selectedItemBasedOnTop.index
                        val offset = abs(state.selectedItemBasedOnTop.offset)
                        val pixelsFromSelected = (stepsFromSelected.toFloat() * state.itemHeight) - offset
                        val maxPixelsFromSelected = (state.viewportHeight / 2) + (state.itemHeight / 2)
                        val mult = (pixelsFromSelected / maxPixelsFromSelected).coerceIn(-1f..1f)

                        scrollEffect(this, index, mult, state)
                    }
            ) {
                content(items[getItemsIndex(index, state, items.size)])
            }
        }
    }
}

@Composable
fun rememberKmpWheelPickerState(
    isInfinite: Boolean = false,
    initiallySelectedItemIndex: Int = 0,
) = rememberSaveable(saver = KMPWheelPickerState.Saver()) {
    KMPWheelPickerState(
        initiallySelectedItemIndex = initiallySelectedItemIndex,
        isInfinite = isInfinite,
    )
}

/**
 * [KMPWheelPickerState] The state for KMPWheelPicker
 */
@Stable
data class KMPWheelPickerState(
    val initiallySelectedItemIndex: Int = 0,
    val isInfinite: Boolean = false,
): ScrollableState {

    internal var lastOnChangeIndex = if (isInfinite) initiallySelectedItemIndex + INFINITE_OFFSET else initiallySelectedItemIndex

    val lazyListState: LazyListState = LazyListState(
        firstVisibleItemIndex = if (isInfinite) {
            INFINITE_OFFSET + initiallySelectedItemIndex
        } else {
            initiallySelectedItemIndex
        }
    )

    val itemHeight by derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.toFloat() ?: 0f }
    val viewportHeight by derivedStateOf { lazyListState.layoutInfo.viewportSize.height.toFloat() }

    val selectedItemRawIndex by derivedStateOf {
        val layoutInfo = lazyListState.layoutInfo
        layoutInfo.visibleItemsInfo.find {
            it.offset + it.size - layoutInfo.viewportStartOffset > layoutInfo.viewportSize.height / 2
        }?.index ?: initiallySelectedItemIndex
    }

    val selectedItemRawIndexFlow = snapshotFlow { selectedItemRawIndex }

    internal val selectedItemBasedOnTop by derivedStateOf {
        val layoutInfo = lazyListState.layoutInfo
        val item = layoutInfo.visibleItemsInfo.firstOrNull {
            it.offset + it.size - layoutInfo.viewportStartOffset > (layoutInfo.viewportSize.height / 2) - (it.size / 2)
        }
        ScrollEffectAnimationData(index = item?.index ?: initiallySelectedItemIndex, offset = item?.offset ?: 0)
    }

    internal val verticalPadding by derivedStateOf {
        val viewportHeight = lazyListState.layoutInfo.viewportSize.height.toFloat()
        val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.toFloat() ?: 0f
        ((viewportHeight - itemHeight) / 2).coerceAtLeast(0f)
    }

    override val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    override val canScrollBackward: Boolean
        get() = lazyListState.canScrollBackward

    override val canScrollForward: Boolean
        get() = lazyListState.canScrollForward

    override suspend fun scroll(scrollPriority: MutatePriority, block: suspend ScrollScope.() -> Unit) =
        lazyListState.scroll(scrollPriority, block)

    override fun dispatchRawDelta(delta: Float): Float = lazyListState.dispatchRawDelta(delta)

    suspend fun scrollToItem(index: Int) {
        lazyListState.scrollToItem(if (isInfinite) index + INFINITE_OFFSET else index)
    }

    /**
     * Animates scrolling to the given index. If the wheel picker is infinite, passing the itemCount will allow the
     * picker to scroll to the nearest matching index
     */
    suspend fun animateScrollToItem(index: Int, itemCount: Int? = null) {
        if (isInfinite && itemCount != null) {
            val indexMultiplier = (selectedItemRawIndex / itemCount)
            val lesser = (indexMultiplier * itemCount) - (itemCount - index)
            val greater = (indexMultiplier * itemCount) + index
            val target = if (abs(selectedItemRawIndex - lesser) < abs(selectedItemRawIndex - greater)) {
                lesser
            } else {
                greater
            }
            lazyListState.animateScrollToItem(target)
        } else {
            lazyListState.animateScrollToItem(if (isInfinite) index + INFINITE_OFFSET else index)
        }
    }

    /**
     * [resetToIndex] Resets internal picker state to the given index and animate scrolls the picker to the correct value
     */
    suspend fun resetToIndex(index: Int, itemCount: Int? = null) {
        lastOnChangeIndex = if (isInfinite) index + INFINITE_OFFSET else index
        animateScrollToItem(index, itemCount)
    }

    companion object {
        fun Saver(): Saver<KMPWheelPickerState, *> = Saver(
            save = { listOf(it.isInfinite, it.selectedItemRawIndex) },
            restore = {
                KMPWheelPickerState(
                    isInfinite = it[0] as Boolean,
                    initiallySelectedItemIndex = it[1] as Int,
                )
            }
        )
    }
}

@Immutable
internal data class ScrollEffectAnimationData(
    val offset: Int,
    val index: Int,
)

typealias KMPWheelPickerScrollEffect =
        GraphicsLayerScope.(index: Int, multiplier: Float, state: KMPWheelPickerState) -> Unit

typealias KMPWheelPickerIndicator =
        ContentDrawScope.(state: KMPWheelPickerState) -> Unit

object KMPWheelPickerIndicators {
    val none: KMPWheelPickerIndicator = { _ -> drawContent() }

    fun window(
        color: Color = Color(0x14747480),
        shape: Shape = RoundedCornerShape(8.dp),
    ): KMPWheelPickerIndicator =
        fun ContentDrawScope.(state: KMPWheelPickerState) {
            val outline = shape.createOutline(
                size = Size(size.width, state.itemHeight),
                layoutDirection = layoutDirection,
                density = Density(density, fontScale)
            )
            val path = Path().apply {
                addOutline(outline)
                translate(Offset(0f, (size.height / 2) - (state.itemHeight / 2)))
            }

            drawPath(path, color)
            drawContent()
        }

    fun bars(
        color: Color = Color(0x14747480),
        thickness: Dp = 2.dp,
    ): KMPWheelPickerIndicator =
        fun ContentDrawScope.(state: KMPWheelPickerState) {
            val y1 = (size.height / 2) - (state.itemHeight / 2)
            val y2 = (size.height / 2) + (state.itemHeight / 2)

            drawLine(
                color = color,
                start = Offset(0f, y1),
                end = Offset(size.width, y1),
                strokeWidth = thickness.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(0f, y2),
                end = Offset(size.width, y2),
                strokeWidth = thickness.toPx(),
                cap = StrokeCap.Round,
            )
            drawContent()
        }
}

object KMPWheelPickerScrollEffects {
    val none: KMPWheelPickerScrollEffect = { _, _ ,_ -> }

    fun wheel(): KMPWheelPickerScrollEffect =
        fun GraphicsLayerScope.(_: Int, multiplier: Float, _: KMPWheelPickerState) {
            rotationX = (70f * multiplier).coerceIn(-360f..360f)
            scaleX = (1f - .15f * abs(multiplier)).coerceIn(0f..1f)
            scaleY = (1f - .15f * abs(multiplier)).coerceIn(0f..1f)
            alpha = (1f - .5f * abs(multiplier)).coerceIn(0f..1f)
            transformOrigin = TransformOrigin(.5f, (.5f - (.7f * multiplier)).coerceIn(0f..1f))
        }

    /**
     * @param itemHorizontalPadding Any horizontal padding applied to the items. This needs to be accounted for when
     * scaling the item horizontally. It is recommended to instead apply the padding on the wheel picker itself
     */
    fun magnify(
        alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        itemHorizontalPadding: Dp = 0.dp,
    ): KMPWheelPickerScrollEffect =
        fun GraphicsLayerScope.(_: Int, multiplier: Float, state: KMPWheelPickerState) {
            val selectionIndicatorMult =
                (multiplier / (1f / (((state.viewportHeight / state.itemHeight) + 1f) / 2f))).coerceIn(-1f..1f)
            scaleX = (1f - .25f * abs(selectionIndicatorMult)).coerceIn(0f..1f)
            scaleY = (1f - .25f * abs(selectionIndicatorMult)).coerceIn(0f..1f)
            alpha = 1f - 1f * abs(multiplier)

            when (alignment) {
                Alignment.Start -> {
                    transformOrigin = TransformOrigin(0f, .5f)
                    translationX = ((itemHorizontalPadding.toPx() - (itemHorizontalPadding.toPx() * scaleX)))
                }
                Alignment.End -> {
                    transformOrigin = TransformOrigin(1f, .5f)
                    translationX = -((itemHorizontalPadding.toPx() - (itemHorizontalPadding.toPx() * scaleX)))
                }
            }
        }
}

private fun getItemsIndex(index: Int, state: KMPWheelPickerState, itemCount: Int) =
    if (state.isInfinite) {
        val mod = ((index - INFINITE_OFFSET) % itemCount)
        if (mod < 0) itemCount + mod else mod
    } else {
        index
    }

private const val INFINITE_OFFSET = Int.MAX_VALUE / 2