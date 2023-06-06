package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent

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
    val contentPadding: Dp = 16.dp,
) {
    companion object {
        val None = BottomSheetStyles(
            maxWidth = Dp.Unspecified,
            shadow = OuterShadow(blur = 0.dp, color = Color.Transparent),
            backgroundColor = Color.Transparent,
            backgroundShape = RectangleShape,
            contentPadding = 0.dp,
        )
    }
}

/**
 * Composes a BottomSheet that can be placed anywhere in the composable tree.
 * Note: [BottomSheet] does not support full screen content and will not draw behind system bars. Use [InlineBottomSheet] for
 * full screen content
 */
@Composable
fun BottomSheet(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
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
        styles = styles,
        content = content,
    )
}

/**
 * Composes a BottomSheet inline with the composable tree. Placement matters with [InlineBottomSheet] and will not render properly
 * if used in the incorrect place in the composable tree.
 * Note: [InlineBottomSheet] supports full screen content and will draw behind system bars.
 */
@Composable
fun InlineBottomSheet(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
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
                        Box(
                            modifier = Modifier
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

private val BottomSheetPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}
