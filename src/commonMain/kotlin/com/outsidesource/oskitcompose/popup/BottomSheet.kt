package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent
import com.outsidesource.oskitcompose.router.KMPBackHandler

@Immutable
data class BottomSheetStyles(
    val transitionDuration: Int = 300,
    val backdropColor: Color = Color.Black.copy(alpha = .5f),
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheet(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    bottomSheetStyles: BottomSheetStyles = remember { BottomSheetStyles() },
    content: @Composable BoxScope.() -> Unit,
) {
    var bottomSheetVisible by remember { mutableStateOf(false) }

    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(bottomSheetStyles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        LaunchedEffect(isVisible) {
            bottomSheetVisible = isVisible
        }

        if (transition.currentState || transition.targetState) {
            Popup(
                popupPositionProvider = BottomSheetPositionProvider,
                focusable = true,
                onDismissRequest = onDismissRequest,
                onKeyEvent = {
                    if (it.key == Key.Escape && shouldDismissOnEscapeKey) onDismissRequest?.invoke()
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
                        .background(color = bottomSheetStyles.backdropColor.copy(bottomSheetStyles.backdropColor.alpha * alpha)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.preventClickPropagationToParent(),
                        visible = bottomSheetVisible,
                        enter = slideInVertically(tween(bottomSheetStyles.transitionDuration)) { it },
                        exit = slideOutVertically(tween(bottomSheetStyles.transitionDuration)) { it }
                    ) {
                        Box(modifier = modifier) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

val BottomSheetPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(0, 0)
    }
}

val defaultBottomSheetModifier = Modifier
    .fillMaxWidth()
    .outerShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )
    .background(
        Color.White,
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )
    .padding(16.dp)
