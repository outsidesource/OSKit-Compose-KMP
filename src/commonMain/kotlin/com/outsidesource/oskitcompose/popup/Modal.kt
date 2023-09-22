package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.disablePointerInput
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent

@Immutable
data class ModalStyles(
    val transitionDuration: Int = 200,
    val scrimColor: Color = Color.Black.copy(alpha = .5f),
    val shadow: OuterShadow = OuterShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(8.dp)
    ),
    val backgroundColor: Color = Color.White,
    val backgroundShape: Shape = RoundedCornerShape(8.dp),
    val windowPadding: PaddingValues = PaddingValues(20.dp),
    val contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    companion object {
        val None = ModalStyles(
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            windowPadding = PaddingValues(0.dp),
            contentPadding = PaddingValues(0.dp),
        )
    }
}


/**
 * Creates a fully customizable [Modal]
 *
 * @param isVisible Whether the modal is visible or not
 * @param onDismissRequest Executes when the user performs an action to dismiss the [Modal]
 * @param shouldDismissOnExternalClick calls [onDismissRequest] when clicking on the scrim
 * @param shouldDismissOnEscapeKey call [onDismissRequest] when pressing escape or back key
 * @param onPreviewKeyEvent Handles the onPreviewKey event
 * @param onKeyEvent Handles the onKeyEvent
 * @param isFullScreen Only utilized in Android. Specifies whether to draw behind the system bars or not
 * @param styles Styles to modify the look of the [Modal]
 * @param content The content to be displayed inside the popup.
 */
@Composable
fun Modal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
    isFullScreen: Boolean = true,
    styles: ModalStyles = remember { ModalStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val transition = updateTransition(isVisible)
    val alpha by transition.animateFloat(
        transitionSpec = { tween(styles.transitionDuration) },
        targetValueByState = { if (it) 1f else 0f },
        label = "AlphaAnimation"
    )
    val scale by transition.animateFloat(
        transitionSpec = { tween(styles.transitionDuration) },
        targetValueByState = { if (it) 1f else .95f },
        label = "ScaleAnimation"
    )
    val translate by transition.animateFloat(
        transitionSpec = { tween(styles.transitionDuration) },
        targetValueByState = { if (it) 0f else -10f * density.density },
        label = "TranslateAnimation"
    )

    if (transition.currentState || transition.targetState) {
        Popup(
            popupPositionProvider = ModalPositionProvider,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            focusable = true,
            isFullScreen = isFullScreen,
            onKeyEvent = {
                if ((it.key == Key.Escape || it.key == Key.Back) && shouldDismissOnEscapeKey) onDismissRequest?.invoke()
                if (onKeyEvent != null) return@Popup onKeyEvent(it)
                false
            }
        ) {
            Box(
                modifier = Modifier
                    .disablePointerInput(!LocalWindowInfo.current.isWindowFocused)
                    .clickable(remember { MutableInteractionSource() }, indication = null) {
                        if (shouldDismissOnExternalClick) onDismissRequest?.invoke()
                    }
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(styles.scrimColor),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .preventClickPropagationToParent()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationY = translate
                        }
                        .padding(styles.windowPadding)
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

private val ModalPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}