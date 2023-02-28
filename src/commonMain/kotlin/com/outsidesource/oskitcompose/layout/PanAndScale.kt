package com.outsidesource.oskitcompose.layout

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.geometry.*
import com.outsidesource.oskitcompose.modifier.kmpMouseScrollFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * UI Tests:
 * 1. Scale out and in
 * 2. Scale partially out, move mouse scale in
 * 3. Scale partially out, pan, scale in
 * 4. Animate pan
 * 5. Spam animation
 * 6. Scale while animating
 */

// TODO: Use rememberSaveable for rememberPanAndScaleState() to work on Android

@Stable
class PanAndScaleState(
    initialScale: Float = 1f,
    initialPan: DpOffset = DpOffset.Zero,
    val minScale: Float = .1f,
    val maxScale: Float = 1f,
) {
    var pan: DpOffset by mutableStateOf(initialPan)
        internal set
    var scale: Float by mutableStateOf(initialScale)
        internal set

    var isAnimatingPan = false
        internal set
    var isAnimatingScale = false
        internal set
    val isAnimating
        get() = isAnimatingPan || isAnimatingScale

    internal var mousePos = DpOffset(0f.dp, 0f.dp)

    private var panAnimationJob: Job? = null
    private var scaleAnimationJob: Job? = null

    /**
     * Pan to a specific location on the canvas. panTo() takes scale into account so the offset provided should treat
     * the offset as if scale is 1.0.
     */
    fun panTo(offset: DpOffset) {
        pan = offset * scale
    }

    suspend fun animatePanTo(offset: DpOffset, animationSpec: AnimationSpec<DpOffset> = tween()): Job = coroutineScope {
        panAnimationJob?.cancelAndJoin()
        scaleAnimationJob?.cancelAndJoin()

        launch {
            isAnimatingPan = true

            animate(DpOffset.VectorConverter, pan, offset * scale, animationSpec = animationSpec) { currentValue, _ ->
                pan = currentValue
            }
        }.apply {
            panAnimationJob = this
            invokeOnCompletion { isAnimatingPan = false }
        }
    }

    fun scaleTo(targetScale: Float, origin: DpOffset = DpOffset.Zero) {
        val adjustedTargetScale = targetScale.coerceIn(minScale, maxScale)
        pan -= ((origin / scale) * (adjustedTargetScale - scale))
        scale = adjustedTargetScale
    }

    suspend fun animateScaleTo(
        targetScale: Float,
        origin: DpOffset = DpOffset.Zero,
        animationSpec: AnimationSpec<Float> = tween(),
    ): Job = coroutineScope {
        scaleAnimationJob?.cancelAndJoin()
        panAnimationJob?.cancelAndJoin()

        launch {
            isAnimatingScale = true
            var previousScale = scale
            val scaledOrigin = origin / scale

            animate(
                previousScale,
                targetScale.coerceIn(minScale, maxScale),
                animationSpec = animationSpec
            ) { currentValue, _ ->
                scale = currentValue
                pan -= ((scaledOrigin) * (currentValue - previousScale))
                previousScale = currentValue
            }
        }.apply {
            scaleAnimationJob
            invokeOnCompletion { isAnimatingScale = false }
        }
    }
}

@Composable
fun PanAndScale(
    modifier: Modifier = Modifier,
    state: PanAndScaleState,
    onClick: ((absoluteMousePos: DpOffset, mousePos: DpOffset) -> Unit)? = null,
    onPan: (DpOffset) -> Unit = {},
    showGrid: Boolean = true,
    gridSize: Dp = 20.dp,
    background: Color,
    gridColor: Color,
    subcomposeLayoutState: SubcomposeLayoutState = remember { SubcomposeLayoutState(0) },
    content: PanAndScaleScope.() -> Unit,
) = SubcomposeLayout(
    modifier = modifier
        .background(background)
        .run {
            if (!showGrid) return@run this
            drawGrid(state, gridSize, gridColor)
        }
        .panAndScalable(state, onPan, onClick),
    state = subcomposeLayoutState,
    measurePolicy = { constraints ->
        val scope = PanAndScaleScopeImp(state, constraints).apply { content() }
        val placeables = arrayOfNulls<Placeable>(scope.contentProvider.count)
        val offsets = arrayOfNulls<DpOffset>(scope.contentProvider.count)
        val forceRenders = arrayOfNulls<Boolean>(scope.contentProvider.count)

        for (i in 0 until scope.contentProvider.count) {
            val offset = scope.contentProvider.offset(i)
            val maxSize = scope.contentProvider.maxSize(i)
            val forceRender = scope.contentProvider.forceRender(i)
            forceRenders[i] = forceRender

            if (!forceRender) {
                val itemRect = DpRect(
                    origin = (state.pan / state.scale) + offset,
                    size = maxSize.copy(
                        width = if (maxSize == DpSize.Unspecified) 0.dp else maxSize.width,
                        height = if (maxSize == DpSize.Unspecified) 0.dp else maxSize.height,
                    )
                )

                if ((maxSize != DpSize.Unspecified && maxSize.width != Dp.Unspecified && itemRect.right.toPx() < 0) ||
                    (maxSize != DpSize.Unspecified && maxSize.height != Dp.Unspecified && itemRect.bottom.toPx() < 0) ||
                    (itemRect.left.toPx() >= (constraints.maxWidth / state.scale)) ||
                    (itemRect.top.toPx() >= (constraints.maxHeight / state.scale))
                ) {
                    continue
                }
            }

            offsets[i] = scope.contentProvider.offset(i)
            val measurable = subcompose(scope.contentProvider.key(i), scope.contentProvider.content(i)).first()

            placeables[i] = measurable.measure(
                Constraints(maxWidth = Constraints.Infinity, maxHeight = Constraints.Infinity)
            )
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, placeable ->
                if (placeable == null) return@forEachIndexed
                val offset = offsets[i] ?: return@forEachIndexed
                val shouldForceRender = forceRenders[i] ?: return@forEachIndexed
                val scaledPosition = (state.pan / state.scale) + offset

                if (!shouldForceRender && ((scaledPosition.x.toPx() + placeable.width < 0) || (scaledPosition.y.toPx() + placeable.height < 0))) {
                    return@forEachIndexed
                }

                placeable.placeRelativeWithLayer(
                    position = IntOffset.Zero,
                    layerBlock = {
                        transformOrigin = TransformOrigin(0f, 0f)
                        translationX = state.pan.x.toPx() + (offset.toIntOffset(density).x * state.scale)
                        translationY = state.pan.y.toPx() + (offset.toIntOffset(density).y * state.scale)
                        scaleX = state.scale
                        scaleY = state.scale
                    }
                )
            }
        }
    }
)

private fun Modifier.drawGrid(state: PanAndScaleState, gridSize: Dp = 20.dp, color: Color) = drawBehind {
    val gridPxSize = gridSize.toPx() * state.scale
    val pxOffset = (state.pan.toOffset(density) % gridPxSize)

    for (x in 0..(size.width / gridPxSize).toInt()) {
        val calcX = (x * gridPxSize)
        drawLine(
            color,
            start = Offset(calcX + pxOffset.x + .5f, 0f),
            end = Offset(calcX + pxOffset.x + .5f, size.height)
        )
    }

    for (y in 0..(size.height / gridPxSize).toInt()) {
        val calcY = (y * gridPxSize)
        drawLine(
            color,
            start = Offset(0f, calcY + pxOffset.y + .5f),
            end = Offset(size.width, calcY + pxOffset.y + .5f)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.panAndScalable(
    state: PanAndScaleState,
    onPan: (DpOffset) -> Unit = {},
    onClick: ((absoluteMousePos: DpOffset, mousePos: DpOffset) -> Unit)? = null,
): Modifier = this
    .pointerInput(state) {
        val allowableClickSlop = 1.dp

        forEachGesture {
            awaitPointerEventScope {
                awaitFirstDown()
                val start = System.currentTimeMillis()
                var slop = DpOffset.Zero

                while (true) {
                    val event = awaitPointerEvent().changes.first()
                    if (event.changedToUp()) {
                        if (slop.x <= allowableClickSlop && slop.y <= allowableClickSlop &&
                            System.currentTimeMillis() - start < 200
                        ) {
                            onClick?.invoke(
                                (event.position.toDpOffset(density) - state.pan) / state.scale,
                                event.position.toDpOffset(density)
                            )
                        }
                        break
                    } else if (event.positionChanged()) {
                        if (state.isAnimatingPan || state.isAnimatingScale) return@awaitPointerEventScope
                        val posChange = event.positionChange().toDpOffset(density)
                        slop += posChange
                        state.pan += posChange
                        onPan(state.pan)
                    }
                }
            }
        }
    }
    .pointerInput(state) {
        forEachGesture {
            awaitPointerEventScope {
                val event = awaitPointerEvent().changes.first()
                state.mousePos = event.position.toDpOffset(density)
            }
        }
    }
    .kmpMouseScrollFilter { _, delta ->
        if (state.isAnimatingPan || state.isAnimatingScale) return@kmpMouseScrollFilter
        val previousScale = state.scale
        state.scale = (state.scale - (delta.y * .025f)).coerceIn(state.minScale, state.maxScale)

        if (state.scale == previousScale) return@kmpMouseScrollFilter
        val origin = (state.mousePos - state.pan) / previousScale
        state.pan -= origin * (state.scale - previousScale)
    }

interface PanAndScaleScope {
    val panAndScaleState: PanAndScaleState
    val pan get() = panAndScaleState.pan
    val scale get() = panAndScaleState.scale
    val constraints: Constraints

    fun items(
        count: Int,
        key: (index: Int) -> Any,
        offset: (index: Int) -> DpOffset,
        maxSize: (index: Int) -> DpSize = { DpSize.Unspecified },
        forceRender: (index: Int) -> Boolean = { false },
        content: @Composable (index: Int) -> Unit,
    )
}

inline fun <T> PanAndScaleScope.items(
    items: List<T>,
    noinline key: (item: T) -> Any,
    crossinline position: (item: T) -> DpOffset,
    crossinline maxSize: (item: T) -> DpSize = { DpSize.Unspecified },
    crossinline shouldForceRender: (item: T) -> Boolean = { false },
    crossinline itemContent: @Composable (item: T) -> Unit,
) = items(
    count = items.size,
    key = { index: Int -> key(items[index]) },
    offset = { index -> position(items[index]) },
    maxSize = { index -> maxSize(items[index]) },
    forceRender = { index -> shouldForceRender(items[index]) },
) {
    itemContent(items[it])
}

private data class PanAndScaleScopeImp(
    override val panAndScaleState: PanAndScaleState,
    override val constraints: Constraints,
) : PanAndScaleScope {

    var contentProvider: PanAndScaleContentProvider = PanAndScaleContentProvider()

    override fun items(
        count: Int,
        key: (index: Int) -> Any,
        offset: (index: Int) -> DpOffset,
        maxSize: (index: Int) -> DpSize,
        forceRender: (index: Int) -> Boolean,
        content: @Composable (index: Int) -> Unit,
    ) {
        contentProvider = PanAndScaleContentProvider(
            count = count,
            key = key,
            offset = { index -> offset(index) },
            content = { index -> @Composable { content(index) } },
            maxSize = { index -> maxSize(index) },
            forceRender = { index -> forceRender(index) }
        )
    }
}

private data class PanAndScaleContentProvider(
    val count: Int = 0,
    val key: (index: Int) -> Any = { Unit },
    val content: (index: Int) -> @Composable () -> Unit = { {} },
    val offset: (index: Int) -> DpOffset = { DpOffset.Zero },
    val maxSize: (index: Int) -> DpSize = { DpSize.Zero },
    val forceRender: (index: Int) -> Boolean = { false },
)
