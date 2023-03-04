package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.modifier.preventClickPropagationToParent
import com.outsidesource.oskitcompose.router.KMPBackHandler

@Immutable
data class DrawerStyles(
    val topMargin: Dp = 0.dp,
    val transitionDuration: Int = 300,
    val backdropColor: Color = Color.Black.copy(alpha = .5f),
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Drawer(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnBackPress: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    drawerStyles: DrawerStyles = remember { DrawerStyles() },
    content: @Composable BoxScope.() -> Unit
) {
    var drawerSheetVisible by remember { mutableStateOf(false) }

    Box {
        val transition = updateTransition(isVisible, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(drawerStyles.transitionDuration) },
            targetValueByState = { if (it) 1f else 0f },
            label = "AlphaAnimation"
        )

        LaunchedEffect(isVisible) {
            drawerSheetVisible = isVisible
        }

        if (transition.currentState || transition.targetState) {
            Popup(
                popupPositionProvider = DrawerPositionProvider,
                focusable = true,
                onKeyEvent = {
                    if (it.key == Key.Escape && shouldDismissOnEscapeKey) onDismissRequest?.invoke()
                    false
                }
            ) {
                KMPBackHandler(enabled = shouldDismissOnBackPress, onBack = { onDismissRequest?.invoke() })

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .layout { measurable, constraints ->
                            val top = drawerStyles.topMargin.toPx().toInt()
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
                        .background(color = drawerStyles.backdropColor.copy(drawerStyles.backdropColor.alpha * alpha)),
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.preventClickPropagationToParent(),
                        visible = drawerSheetVisible,
                        enter = slideInHorizontally(tween(drawerStyles.transitionDuration)) { -it },
                        exit = slideOutHorizontally(tween(drawerStyles.transitionDuration)) { -it }
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

val DrawerPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset(0, 0)
}

val defaultDrawerModifier = Modifier
    .width(300.dp)
    .fillMaxHeight()
    .outerShadow(
        blur = 11.dp,
        color = Color.Black.copy(alpha = .25f),
        shape = RoundedCornerShape(16.dp)
    )
    .background(
        color = Color.White,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    )
    .padding(16.dp)