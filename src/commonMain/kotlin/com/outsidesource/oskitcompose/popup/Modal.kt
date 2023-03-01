package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.modifier.consumePointerInput
import com.outsidesource.oskitcompose.modifier.disablePointerInput

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BaseModal(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    isDismissibleOnExternalClick: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val modalPositionProvider = remember { ModalPositionProvider() }
    val transition = updateTransition(isVisible)
    val alpha by transition.animateFloat(
        transitionSpec = { tween(200) },
        targetValueByState = { if (it) 1f else 0f },
        label = "AlphaAnimation"
    )
    val scale by transition.animateFloat(
        transitionSpec = { tween(200) },
        targetValueByState = { if (it) 1f else .95f },
        label = "ScaleAnimation"
    )
    val translate by transition.animateFloat(
        transitionSpec = { tween(200) },
        targetValueByState = { if (it) 0f else -10f * density.density },
        label = "TranslateAnimation"
    )

    if (transition.currentState || transition.targetState) {
        Popup(
            popupPositionProvider = modalPositionProvider,
            focusable = true,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = {
                if (it.key == Key.Escape) onDismissRequest?.invoke()
                return@Popup onKeyEvent(it)
            }
        ) {
            Box(
                modifier = Modifier
                    .disablePointerInput(!LocalWindowInfo.current.isWindowFocused)
                    .clickable(remember { MutableInteractionSource() }, indication = null) {
                        if (isDismissibleOnExternalClick) onDismissRequest?.invoke()
                    }
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .consumePointerInput()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationY = translate
                        }
                ) {
                    content()
                }
            }
        }
    }
}

private class ModalPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}