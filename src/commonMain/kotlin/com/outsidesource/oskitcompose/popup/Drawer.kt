package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
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
    val contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    companion object {
        val None = DrawerStyles(
            width = Dp.Unspecified,
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
        )
    }
}

/**
 * Creates a fully customizable [Drawer]
 *
 * @param isVisible Whether the modal is visible or not
 * @param onDismissRequest Executes when the user performs an action to dismiss the [Drawer]
 * @param shouldDismissOnExternalClick calls [onDismissRequest] when clicking on the scrim
 * @param shouldDismissOnEscapeKey call [onDismissRequest] when pressing escape or back key
 * @param shouldDismissOnSwipe calls [onDismissRequest] when swiping the bottom sheet away
 * @param isFullScreen Only utilized in Android. Specifies whether to draw behind the system bars or not
 * @param styles Styles to modify the look of the [Drawer]
 * @param content The content to be displayed inside the popup.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Drawer(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    shouldDismissOnSwipe: Boolean = true,
    isFullScreen: Boolean = true,
    styles: DrawerStyles = remember { DrawerStyles() },
    content: @Composable BoxScope.() -> Unit
) {
    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(styles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        if (transition.currentState || transition.targetState) {
            Popup(
                popupPositionProvider = DrawerPositionProvider,
                focusable = true,
                onDismissRequest = onDismissRequest,
                isFullScreen = isFullScreen,
                onKeyEvent = {
                    if ((it.key == Key.Escape || it.key == Key.Back) && shouldDismissOnEscapeKey) {
                        onDismissRequest?.invoke()
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
                    val density = LocalDensity.current
                    val swipeData = remember { DrawerSwipeData() }
                    val dismissData = remember(onDismissRequest, styles.transitionDuration) {
                        DrawerDismissData(
                            onDismissRequest = onDismissRequest,
                            transitionDuration = styles.transitionDuration,
                        )
                    }
                    val isDragging by swipeData.isDragging
                    val offset by swipeData.offset
                    val offsetAnim = swipeData.offsetAnim

                    LaunchedEffect(isVisible) {
                        if (isVisible) {
                            offsetAnim.snapTo(-swipeData.size.value.width.toFloat())
                            offsetAnim.animateTo(0f, tween(styles.transitionDuration))
                        } else if (!offsetAnim.isRunning) {
                            offsetAnim.animateTo(
                                -swipeData.size.value.width.toFloat(),
                                tween(styles.transitionDuration)
                            )
                        }
                    }

                    CompositionLocalProvider(
                        LocalDrawerSwipeData provides swipeData,
                        LocalDrawerDismissData provides dismissData,
                    ) {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { swipeData.size.value = it.size }
                                .then(if (shouldDismissOnSwipe) Modifier.drawerSwipeToDismiss() else Modifier)
                                .offset(x = with(density) { if (isDragging) offset.toDp() else offsetAnim.value.toDp() })
                                .width(styles.width)
                                .fillMaxHeight()
                                .outerShadow(
                                    blur = styles.shadow.blur,
                                    color = styles.shadow.color,
                                    shape = styles.shadow.shape,
                                    spread = styles.shadow.spread,
                                    offset = styles.shadow.offset,
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

private fun Modifier.drawerSwipeToDismiss() = composed {
    val swipeData = LocalDrawerSwipeData.current
    val dismissData = LocalDrawerDismissData.current
    var isDragging by swipeData.isDragging
    var offset by swipeData.offset
    val offsetAnim = swipeData.offsetAnim
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

                    val velocity = velocityTracker.calculateVelocity().x
                    if (velocity < -3250) {
                        dismissData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(-swipeData.size.value.width.toFloat(), initialVelocity = velocity)
                        return@launch
                    } else if (offset.absoluteValue > swipeData.size.value.width / 2) {
                        dismissData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(-swipeData.size.value.width.toFloat(), tween(dismissData.transitionDuration))
                        return@launch
                    }

                    offset = 0f
                    offsetAnim.animateTo(0f, tween(dismissData.transitionDuration))
                }
            }
        )
    }
}

private val LocalDrawerSwipeData = staticCompositionLocalOf { DrawerSwipeData() }
private val LocalDrawerDismissData = staticCompositionLocalOf { DrawerDismissData() }

private data class DrawerSwipeData(
    val offset: MutableState<Float> = mutableStateOf(0f),
    val offsetAnim: Animatable<Float, AnimationVector1D> = Animatable(-Float.MAX_VALUE),
    val isDragging: MutableState<Boolean> = mutableStateOf(false),
    val size: VarRef<IntSize> = VarRef(IntSize.Zero),
)

private data class DrawerDismissData(
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