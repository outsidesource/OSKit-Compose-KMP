package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.geometry.Constraint
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.disablePointerInput
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent

@Immutable
data class ModalStyles(
    val transitionDuration: Int = 200,
    val scrimColor: Color = Color.Black.copy(alpha = .5f),
    val width: Constraint = Constraint(min = 300.dp, max = 600.dp),
    val height: Constraint = Constraint(min = 200.dp, max = 600.dp),
    val shadow: OuterShadow = OuterShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(8.dp)
    ),
    val backgroundColor: Color = Color.White,
    val backgroundShape: Shape = RoundedCornerShape(8.dp),
    val contentPadding: Dp = 16.dp,
) {
    companion object {
        val None = ModalStyles(
            width = Constraint(),
            height = Constraint(),
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = 0.dp,
        )
    }
}

/**
 * Composes a Modal that can be placed anywhere in the composable tree.
 * Note: [Modal] does not support full screen content and will not draw behind system bars. Use [InlineModal] for
 * full screen content
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
    styles: ModalStyles = remember { ModalStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    InternalModal(
        modifier = modifier,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        styles = styles,
        isInline = false,
        content = content,
    )
}

/**
 * Composes a Modal inline with the composable tree. Placement matters with [InlineModal] and will not render properly
 * if used in the incorrect place in the composable tree.
 * Note: [InlineModal] supports full screen content and will draw behind system bars.
 */
@Composable
fun InlineModal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
    styles: ModalStyles = remember { ModalStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    InternalModal(
        modifier = modifier,
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        shouldDismissOnExternalClick = shouldDismissOnExternalClick,
        shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        styles = styles,
        isInline = true,
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InternalModal(
    isInline: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
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
        PopupOrInline(
            isInline = isInline,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = {
                if ((it.key == Key.Escape || it.key == Key.Back) && shouldDismissOnEscapeKey) {
                    onDismissRequest?.invoke()
                    if (isInline) return@PopupOrInline true
                }
                if (onKeyEvent != null) return@PopupOrInline onKeyEvent(it)
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
                        .widthIn(styles.width.min, styles.width.max)
                        .heightIn(styles.height.min, styles.height.max)
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

@Composable
private fun PopupOrInline(
    isInline: Boolean,
    onDismissRequest: (() -> Unit)?,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
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
            popupPositionProvider = ModalPositionProvider,
            focusable = true,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = content
        )
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