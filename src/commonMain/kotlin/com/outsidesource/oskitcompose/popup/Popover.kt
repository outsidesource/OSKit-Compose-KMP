package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.geometry.toIntOffset
import com.outsidesource.oskitcompose.modifier.disablePointerInput

const val popoverAnimDuration = 150

/**
 * PopoverAnchors declares the two points that meet when showing a popup. For example, if parent = Alignment.BottomStart and
 * popover = Alignment.TopStart, the popover's top left corner will meet the parents bottom left corner
 */
data class PopoverAnchors(
    val parent: Alignment,
    val popover: Alignment,
) {
    companion object {
        val InternalTopStart = PopoverAnchors(parent = Alignment.TopStart, popover = Alignment.TopStart)
        val InternalTopCenter = PopoverAnchors(parent = Alignment.TopCenter, popover = Alignment.TopCenter)
        val InternalTopEnd = PopoverAnchors(parent = Alignment.TopEnd, popover = Alignment.TopEnd)
        val InternalCenterStart = PopoverAnchors(parent = Alignment.CenterStart, popover = Alignment.CenterStart)
        val Center = PopoverAnchors(parent = Alignment.Center, popover = Alignment.Center)
        val InternalCenterEnd = PopoverAnchors(parent = Alignment.CenterEnd, popover = Alignment.CenterEnd)
        val InternalBottomStart = PopoverAnchors(parent = Alignment.BottomStart, popover = Alignment.BottomStart)
        val InternalBottomCenter = PopoverAnchors(parent = Alignment.BottomCenter, popover = Alignment.BottomCenter)
        val InternalBottomEnd = PopoverAnchors(parent = Alignment.BottomEnd, popover = Alignment.BottomEnd)

        val ExternalBottomAlignStart = PopoverAnchors(parent = Alignment.BottomStart, popover = Alignment.TopStart)
        val ExternalBottomAlignCenter = PopoverAnchors(parent = Alignment.BottomCenter, popover = Alignment.TopCenter)
        val ExternalBottomAlignEnd = PopoverAnchors(parent = Alignment.BottomEnd, popover = Alignment.TopEnd)
        val ExternalTopAlignStart = PopoverAnchors(parent = Alignment.TopStart, popover = Alignment.BottomStart)
        val ExternalTopAlignCenter = PopoverAnchors(parent = Alignment.TopCenter, popover = Alignment.BottomCenter)
        val ExternalTopAlignEnd = PopoverAnchors(parent = Alignment.TopEnd, popover = Alignment.BottomEnd)

        val ExternalLeftAlignTop = PopoverAnchors(parent = Alignment.TopStart, popover = Alignment.TopEnd)
        val ExternalLeftAlignCenter = PopoverAnchors(parent = Alignment.CenterStart, popover = Alignment.CenterEnd)
        val ExternalLeftAlignBottom = PopoverAnchors(parent = Alignment.BottomStart, popover = Alignment.BottomEnd)
        val ExternalRightAlignTop = PopoverAnchors(parent = Alignment.TopEnd, popover = Alignment.TopStart)
        val ExternalRightAlignCenter = PopoverAnchors(parent = Alignment.CenterEnd, popover = Alignment.CenterStart)
        val ExternalRightAlignBottom = PopoverAnchors(parent = Alignment.BottomEnd, popover = Alignment.BottomStart)
    }
}

@Composable
fun Popover(
    isVisible: Boolean,
    anchors: PopoverAnchors = PopoverAnchors.ExternalBottomAlignStart,
    offset: DpOffset = DpOffset(0f.dp, 0f.dp),
    popupPositionProvider: PopupPositionProvider? = null,
    onDismissRequest: (() -> Unit)? = null,
    dismissOnBackKey: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusable: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val popoverPositionProvider = popupPositionProvider ?: remember(anchors, offset, density) {
        PopoverPositionProvider(anchors, offset, density)
    }
    val transition = updateTransition(isVisible)
    val alpha by transition.animateFloat(
        transitionSpec = { tween(popoverAnimDuration) },
        targetValueByState = { if (it) 1f else 0f },
        label = "AlphaAnimation"
    )
    val scale by transition.animateFloat(
        transitionSpec = { tween(popoverAnimDuration) },
        targetValueByState = { if (it) 1f else .95f },
        label = "ScaleAnimation"
    )
    val translate by transition.animateFloat(
        transitionSpec = { tween(popoverAnimDuration) },
        targetValueByState = { if (it) 0f else -10f * density.density },
        label = "TranslateAnimation"
    )

    if (transition.currentState || transition.targetState) {
        KmpPopup(
            popupPositionProvider = popoverPositionProvider,
            focusable = focusable,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            dismissOnBackPress = dismissOnBackKey,
            onKeyEvent = onKeyEvent,
            isFullScreen = false,
        ) {
            Box(
                modifier = Modifier
                    .disablePointerInput(!LocalWindowInfo.current.isWindowFocused)
                    .graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                        this.translationY = translate
                    },
                content = content,
            )
        }
    }
}

@Composable
fun <T> Popover(
    isVisible: Boolean,
    anchors: PopoverAnchors = PopoverAnchors.ExternalBottomAlignStart,
    offset: DpOffset = DpOffset(0f.dp, 0f.dp),
    popupPositionProvider: PopupPositionProvider? = null,
    onDismissRequest: (() -> Unit)? = null,
    dismissOnBackKey: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusable: Boolean = true,
    transitionValueCreator: @Composable (transition: Transition<Boolean>) -> T,
    content: @Composable BoxScope.(T) -> Unit
) {
    val density = LocalDensity.current
    val popoverPositionProvider = popupPositionProvider ?: remember(anchors, offset, density) {
        PopoverPositionProvider(anchors, offset, density)
    }
    val transition = updateTransition(isVisible)
    val animationValues = transitionValueCreator(transition)

    if (transition.currentState || transition.targetState) {
        KmpPopup(
            popupPositionProvider = popoverPositionProvider,
            focusable = focusable,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            dismissOnBackPress = dismissOnBackKey,
            onKeyEvent = onKeyEvent,
            isFullScreen = false,
        ) {
            Box(
                modifier = Modifier
                    .disablePointerInput(!LocalWindowInfo.current.isWindowFocused),
                content = {
                    content(animationValues)
                },
            )
        }
    }
}

class PopoverPositionProvider(
    private val anchors: PopoverAnchors,
    private val offset: DpOffset,
    private val density: Density
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val parentAnchorOffset = when (anchors.parent) {
            Alignment.TopStart -> anchorBounds.topLeft
            Alignment.TopCenter -> anchorBounds.topCenter
            Alignment.TopEnd -> anchorBounds.topRight
            Alignment.CenterStart -> anchorBounds.centerLeft
            Alignment.Center -> anchorBounds.center
            Alignment.CenterEnd -> anchorBounds.centerRight
            Alignment.BottomStart -> anchorBounds.bottomLeft
            Alignment.BottomCenter -> anchorBounds.bottomCenter
            Alignment.BottomEnd -> anchorBounds.bottomRight
            else -> IntOffset.Zero
        }
        val popupAnchorOffset = when (anchors.popover) {
            Alignment.TopStart -> IntOffset.Zero
            Alignment.TopCenter -> IntOffset(popupContentSize.width / -2, 0)
            Alignment.TopEnd -> IntOffset(popupContentSize.width * -1, 0)
            Alignment.CenterStart -> IntOffset(0, popupContentSize.height / -2)
            Alignment.Center -> IntOffset(popupContentSize.width / -2, popupContentSize.height / -2)
            Alignment.CenterEnd -> IntOffset(popupContentSize.width * -1, popupContentSize.height / -2)
            Alignment.BottomStart -> IntOffset(0, popupContentSize.height * -1)
            Alignment.BottomCenter -> IntOffset(popupContentSize.width / -2, popupContentSize.height * -1)
            Alignment.BottomEnd -> IntOffset(popupContentSize.width * -1, popupContentSize.height * -1)
            else -> IntOffset.Zero
        }

        val initialOffset = parentAnchorOffset + popupAnchorOffset + offset.toIntOffset(density)
        if (isOffsetInBounds(initialOffset, windowSize, popupContentSize)) return initialOffset

        // Popup does not fit without adjustment
        val adjust = getAdjustment(initialOffset, windowSize, popupContentSize)
        return initialOffset + adjust
    }

    private fun isOffsetInBounds(offset: IntOffset, windowSize: IntSize, popupContentSize: IntSize): Boolean =
        offset.x > 0 &&
            offset.y > 0 &&
            offset.x + popupContentSize.width < windowSize.width &&
            offset.y + popupContentSize.height < windowSize.height

    private fun getAdjustment(offset: IntOffset, windowSize: IntSize, popupContentSize: IntSize): IntOffset {
        var xAdjust = 0
        var yAdjust = 0

        if (offset.x + popupContentSize.width > windowSize.width) {
            xAdjust += windowSize.width - (offset.x + popupContentSize.width)
        } else if (offset.x < 0) {
            xAdjust -= offset.x
        }

        if (offset.y + popupContentSize.height > windowSize.height) {
            yAdjust += windowSize.height - (offset.y + popupContentSize.height)
        } else if (offset.y < 0) {
            yAdjust -= offset.y
        }

        return IntOffset(xAdjust, yAdjust)
    }
}
