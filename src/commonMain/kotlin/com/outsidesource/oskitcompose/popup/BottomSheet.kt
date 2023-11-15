package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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
        /**
         * BottomSheetStyles with all content set to unspecified to allow for custom user definition
         */
        val UserDefinedContent = BottomSheetStyles(
            maxWidth = Dp.Unspecified,
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
        )
    }
}

/**
 * Creates a fully customizable [BottomSheet]
 *
 * @param isVisible Whether the modal is visible or not
 * @param onDismissRequest Executes when the user performs an action to dismiss the [BottomSheet]
 * @param dismissOnExternalClick calls [onDismissRequest] when clicking on the scrim
 * @param dismissOnBackPress call [onDismissRequest] when pressing escape or back key
 * @param dismissOnSwipe calls [onDismissRequest] when swiping the bottom sheet away
 * @param isFullScreen Utilized in Android and iOS. Specifies whether to draw behind the system bars or not
 * @param styles Styles to modify the look of the [BottomSheet]
 * @param content The content to be displayed inside the popup.
 */
@Composable
fun BottomSheet(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    dismissOnExternalClick: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnSwipe: Boolean = true,
    isFullScreen: Boolean = true,
    styles: BottomSheetStyles = remember { BottomSheetStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(styles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        if (transition.currentState || transition.targetState) {
            KMPPopup(
                popupPositionProvider = BottomSheetPositionProvider,
                isFullScreen = isFullScreen,
                onDismissRequest = onDismissRequest,
                dismissOnBackPress = dismissOnBackPress,
                focusable = true,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { if (dismissOnExternalClick) onDismissRequest?.invoke() }
                        )
                        .background(color = styles.scrimColor.copy(styles.scrimColor.alpha * alpha)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val density = LocalDensity.current
                    val swipeData = remember { BottomSheetSwipeData() }
                    val dismissData = remember(onDismissRequest, styles.transitionDuration) {
                        BottomSheetDismissData(
                            onDismissRequest = onDismissRequest,
                            transitionDuration = styles.transitionDuration,
                        )
                    }
                    val isDragging by swipeData.isDragging
                    val offset by swipeData.offset
                    val offsetAnim = swipeData.offsetAnim

                    LaunchedEffect(isVisible) {
                        if (isVisible) {
                            offsetAnim.snapTo(swipeData.size.value.height.toFloat())
                            offsetAnim.animateTo(0f, tween(styles.transitionDuration))
                        } else if (!offsetAnim.isRunning) {
                            offsetAnim.animateTo(
                                swipeData.size.value.height.toFloat(),
                                tween(styles.transitionDuration)
                            )
                        }
                    }

                    CompositionLocalProvider(
                        LocalBottomSheetSwipeData provides swipeData,
                        LocalBottomSheetDismissData provides dismissData,
                    ) {
                        Box(
                            modifier = Modifier
                                .preventClickPropagationToParent()
                                .onGloballyPositioned { swipeData.size.value = it.size }
                                .then(if (dismissOnSwipe) Modifier.bottomSheetSwipeToDismiss() else Modifier)
                                .offset(y = with(density) { if (isDragging) offset.toDp() else offsetAnim.value.toDp() })
                                .widthIn(max = styles.maxWidth)
                                .fillMaxWidth()
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

/**
 * Use to allow a bottom sheet to be swiped to dismiss. This can be used in place of [shouldDismissOnSwipe] to provide
 * a specific swipe handle to the user.
 */
fun Modifier.bottomSheetSwipeToDismiss() = composed {
    val swipeData = LocalBottomSheetSwipeData.current
    val dismissData = LocalBottomSheetDismissData.current
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
                offset = (offset + delta.y).coerceAtLeast(0f)
            },
            onDragEnd = {
                scope.launch {
                    isDragging = false
                    offsetAnim.snapTo(offset)

                    val velocity = velocityTracker.calculateVelocity().y
                    if (velocity > 3250) {
                        dismissData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(
                            targetValue = swipeData.size.value.height.toFloat(),
                            initialVelocity = velocity,
                        )
                        return@launch
                    } else if (offset > swipeData.size.value.height / 2) {
                        dismissData.onDismissRequest?.invoke()
                        offsetAnim.animateTo(
                            targetValue = swipeData.size.value.height.toFloat(),
                            animationSpec = tween(dismissData.transitionDuration)
                        )
                        return@launch
                    }

                    offset = 0f
                    offsetAnim.animateTo(0f, tween(dismissData.transitionDuration))
                }
            }
        )
    }
}

private val LocalBottomSheetSwipeData = staticCompositionLocalOf { BottomSheetSwipeData() }
private val LocalBottomSheetDismissData = staticCompositionLocalOf { BottomSheetDismissData() }

private data class BottomSheetSwipeData(
    val offset: MutableState<Float> = mutableStateOf(0f),
    val offsetAnim: Animatable<Float, AnimationVector1D> = Animatable(Float.MAX_VALUE),
    val isDragging: MutableState<Boolean> = mutableStateOf(false),
    val size: VarRef<IntSize> = VarRef(IntSize.Zero),
)

private data class BottomSheetDismissData(
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
