package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.lib.VarRef
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent
import kotlinx.coroutines.launch

@Immutable
data class BottomSheetStyles(
    val transitionDuration: Int = 300,
    val scrimColor: Color = Color.Black.copy(alpha = .5f),
    val maxWidth: Dp = 500.dp,
    val shadow: OuterShadow = OuterShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ),
    val backgroundColor: Color = Color.White,
    val backgroundShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    companion object {
        val None = BottomSheetStyles(
            maxWidth = Dp.Unspecified,
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
        )
    }
}

/**
 * Composes a BottomSheet that can be placed anywhere in the composable tree.
 * Note: [BottomSheetPopup] does not support full screen content and will not draw behind system bars. Use [BottomSheet] for
 * full screen content
 */
@Composable
fun BottomSheetPopup(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: BottomSheetStyles = remember { BottomSheetStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    InternalBottomSheet(
        modifier = modifier,
        isInline = false,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        shouldDismissOnSwipe = shouldDismissOnSwipe,
        styles = styles,
        content = content,
    )
}

/**
 * Composes a BottomSheet inline with the composable tree. Placement matters with [BottomSheet] and will not render properly
 * if used in the incorrect place in the composable tree.
 * Note: [BottomSheet] supports full screen content and will draw behind system bars.
 */
@Composable
fun BottomSheet(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: BottomSheetStyles = remember { BottomSheetStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    InternalBottomSheet(
        modifier = modifier,
        isInline = true,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        shouldDismissOnSwipe = shouldDismissOnSwipe,
        styles = styles,
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InternalBottomSheet(
    isInline: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: BottomSheetStyles = remember { BottomSheetStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    var bottomSheetVisible by remember { mutableStateOf(false) }

    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(styles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        LaunchedEffect(isVisible) {
            bottomSheetVisible = isVisible
        }

        if (transition.currentState || transition.targetState) {
            PopupOrInline(
                isInline = isInline,
                onDismissRequest = onDismissRequest,
                onKeyEvent = {
                    if ((it.key == Key.Escape || it.key == Key.Back) && shouldDismissOnEscapeKey) {
                        onDismissRequest?.invoke()
                        if (isInline) return@PopupOrInline true
                    }
                    false
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { if (shouldDismissOnExternalClick) onDismissRequest?.invoke() }
                        )
                        .background(color = styles.scrimColor.copy(styles.scrimColor.alpha * alpha)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.preventClickPropagationToParent(),
                        visible = bottomSheetVisible,
                        enter = slideInVertically(tween(styles.transitionDuration)) { it },
                        exit = slideOutVertically(tween(styles.transitionDuration)) { it }
                    ) {
                        val density = LocalDensity.current
                        val handleData = remember(onDismissRequest, styles.transitionDuration) {
                            BottomSheetSwipeHandleData(
                                onDismissRequest = onDismissRequest,
                                transitionDuration = styles.transitionDuration)
                        }
                        val isDragging by handleData.isDragging
                        val offset by handleData.offset
                        val offsetAnim = handleData.offsetAnim

                        CompositionLocalProvider(LocalBottomSheetSwipeHandleData provides handleData) {
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { handleData.size.value = it.size }
                                    .then(if (shouldDismissOnSwipe) Modifier.bottomSheetSwipeToDismiss() else Modifier)
                                    .offset(y = with(density) { if (isDragging) offset.toDp() else offsetAnim.value.toDp() })
                                    .widthIn(max = styles.maxWidth)
                                    .fillMaxWidth()
                                    .outerShadow(
                                        blur = styles.shadow.blur,
                                        color = styles.shadow.color,
                                        shape = styles.shadow.shape,
                                    )
                                    .background(
                                        styles.backgroundColor,
                                        styles.backgroundShape
                                    )
                                    .padding(styles.contentPadding)
                                    .then(modifier)
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupOrInline(
    isInline: Boolean,
    onDismissRequest: (() -> Unit)?,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable () -> Unit,
) {
    if (isInline) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent(onKeyEvent)
        ) {
            content()
        }
    } else {
        Popup(
            popupPositionProvider = BottomSheetPositionProvider,
            focusable = true,
            onDismissRequest = onDismissRequest,
            onKeyEvent = onKeyEvent,
            content = content,
        )
    }
}

fun Modifier.bottomSheetSwipeToDismiss() = composed {
    val handleData = LocalBottomSheetSwipeHandleData.current
    var isDragging by handleData.isDragging
    var offset by handleData.offset
    val offsetAnim = handleData.offsetAnim
    val scope = rememberCoroutineScope()
    val velocityTracker = remember { VelocityTracker() }

    pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                isDragging = true
                velocityTracker.resetTracking()
            },
            onDrag = { change, delta ->
                velocityTracker.addPointerInputChange(change)
                offset = (offset + delta.y).coerceAtLeast(0f)
            },
            onDragEnd = {
                scope.launch {
                    isDragging = false
                    offsetAnim.snapTo(offset)

                    val velocity = velocityTracker.calculateVelocity().y
                    if (velocity > 3250 || offset > handleData.size.value.height / 2) {
                        handleData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(handleData.size.value.height.toFloat(), initialVelocity = velocity)
                        return@launch
                    }

                    offset = 0f
                    offsetAnim.animateTo(0f, tween(handleData.transitionDuration))
                }
            }
        )
    }
}

private val LocalBottomSheetSwipeHandleData = staticCompositionLocalOf { BottomSheetSwipeHandleData() }

private data class BottomSheetSwipeHandleData(
    val offset: MutableState<Float> = mutableStateOf(0f),
    val offsetAnim: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val isDragging: MutableState<Boolean> = mutableStateOf(false),
    val size: VarRef<IntSize> = VarRef(IntSize.Zero),
    val onDismissRequest: (() -> Unit)? = null,
    val transitionDuration: Int = 300,
)

private val BottomSheetPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}
