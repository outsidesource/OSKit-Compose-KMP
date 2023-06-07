package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.lib.VarRef
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Immutable
data class DrawerStyles(
    val topMargin: Dp = 0.dp,
    val transitionDuration: Int = 300,
    val scrimColor: Color = Color.Black.copy(alpha = .5f),
    val width: Dp = 300.dp,
    val shadow: OuterShadow = OuterShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ),
    val backgroundColor: Color = Color.White,
    val backgroundShape: Shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    val contentPadding: Dp = 16.dp,
) {
    companion object {
        val None = DrawerStyles(
            width = Dp.Unspecified,
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = 0.dp,
        )
    }
}

/**
 * Composes a Drawer that can be placed anywhere in the composable tree.
 * Note: [Drawer] does not support full screen content and will not draw behind system bars. Use [InlineDrawer] for
 * full screen content
 */
@Composable
fun Drawer(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: DrawerStyles = remember { DrawerStyles() },
    content: @Composable BoxScope.() -> Unit
) {
    InternalDrawer(
        modifier = modifier,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        shouldDismissOnSwipe = shouldDismissOnSwipe,
        styles = styles,
        content = content,
        isInline = false,
    )
}

/**
 * Composes a Drawer inline with the composable tree. Placement matters with [InlineDrawer] and will not render properly
 * if used in the incorrect place in the composable tree.
 * Note: [InlineDrawer] supports full screen content and will draw behind system bars.
 */
@Composable
fun InlineDrawer(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: DrawerStyles = remember { DrawerStyles() },
    content: @Composable BoxScope.() -> Unit
) {
    InternalDrawer(
        modifier = modifier,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        shouldDismissOnSwipe = shouldDismissOnSwipe,
        styles = styles,
        content = content,
        isInline = true,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InternalDrawer(
    isInline: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    styles: DrawerStyles = remember { DrawerStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    var drawerSheetVisible by remember { mutableStateOf(false) }

    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(styles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        LaunchedEffect(isVisible) {
            drawerSheetVisible = isVisible
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
                        .layout { measurable, constraints ->
                            val top = styles.topMargin.toPx().toInt()
                            val placeable = measurable.measure(
                                constraints.copy(
                                    minHeight = kotlin.math.max(constraints.minHeight - top, 0),
                                    maxHeight = constraints.maxHeight - top
                                ),
                            )

                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.placeRelative(0, top)
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { if (shouldDismissOnExternalClick) onDismissRequest?.invoke() }
                        )
                        .background(color = styles.scrimColor.copy(styles.scrimColor.alpha * alpha)),
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.preventClickPropagationToParent(),
                        visible = drawerSheetVisible,
                        enter = slideInHorizontally(tween(styles.transitionDuration)) { -it },
                        exit = slideOutHorizontally(tween(styles.transitionDuration)) { -it }
                    ) {
                        val density = LocalDensity.current
                        val handleData = remember(onDismissRequest, styles.transitionDuration) {
                            DrawerSwipeHandleData(
                                onDismissRequest = onDismissRequest,
                                transitionDuration = styles.transitionDuration)
                        }
                        val isDragging by handleData.isDragging
                        val offset by handleData.offset
                        val offsetAnim = handleData.offsetAnim

                        CompositionLocalProvider(LocalDrawerSwipeHandleData provides handleData) {
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { handleData.size.value = it.size }
                                    .then(if (shouldDismissOnSwipe) Modifier.drawerSwipeToDismiss() else Modifier)
                                    .offset(x = with(density) { if (isDragging) offset.toDp() else offsetAnim.value.toDp() })
                                    .width(styles.width)
                                    .fillMaxHeight()
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
            popupPositionProvider = DrawerPositionProvider,
            focusable = true,
            onDismissRequest = onDismissRequest,
            onKeyEvent = onKeyEvent,
            content = content
        )
    }
}

private fun Modifier.drawerSwipeToDismiss() = composed {
    val handleData = LocalDrawerSwipeHandleData.current
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
                offset = (offset + delta.x).coerceAtMost(0f)
            },
            onDragEnd = {
                scope.launch {
                    isDragging = false
                    offsetAnim.snapTo(offset)

                    val velocity = velocityTracker.calculateVelocity().y
                    if (velocity > 3250 || offset.absoluteValue > handleData.size.value.width / 2) {
                        handleData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(-handleData.size.value.width.toFloat(), initialVelocity = velocity)
                        return@launch
                    }

                    offset = 0f
                    offsetAnim.animateTo(0f, tween(handleData.transitionDuration))
                }
            }
        )
    }
}

private val LocalDrawerSwipeHandleData = staticCompositionLocalOf { DrawerSwipeHandleData() }

private data class DrawerSwipeHandleData(
    val offset: MutableState<Float> = mutableStateOf(0f),
    val offsetAnim: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val isDragging: MutableState<Boolean> = mutableStateOf(false),
    val size: VarRef<IntSize> = VarRef(IntSize.Zero),
    val onDismissRequest: (() -> Unit)? = null,
    val transitionDuration: Int = 300,
)

private val DrawerPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}