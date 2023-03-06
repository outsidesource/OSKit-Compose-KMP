package com.outsidesource.oskitcompose.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider

typealias BottomSheetState = MutableTransitionState<Boolean>


@Composable
fun BottomSheetControl(
    modifier: Modifier = defaultBottomSheetModifier,
    controlContent: @Composable (BottomSheetState) -> Unit,
    closeButton: @Composable (BottomSheetState) -> Unit = {},
    bottomSheetContent: @Composable () -> Unit,
) {
    val bottomSheetState = remember { MutableTransitionState(false) }
    var bottomSheetVisible by remember { mutableStateOf(false) }


    Box {
        controlContent(bottomSheetState)

        val transition = updateTransition(bottomSheetState, label = "background")
        val alpha by transition.animateFloat(
            transitionSpec = { tween(300) },
            targetValueByState = { if (it) .5f else 0f },
            label = "AlphaAnimation"
        )

        LaunchedEffect(bottomSheetState.targetState) {
            bottomSheetVisible = bottomSheetState.targetState
        }

        if (transition.currentState || transition.targetState) {
            Popup(popupPositionProvider = BottomSheetPositionProvider) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .background(color = Color.Black.copy(alpha = alpha)),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    AnimatedVisibility(
                        visible = bottomSheetVisible,
                        enter = slideInVertically(tween(500)) { it },
                        exit = slideOutVertically(tween(500)) { it }
                    ) {
                        Box(
                            modifier = modifier
                        ) {
//                            closeButton(bottomSheetState)
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                bottomSheetContent()
                            }
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

private val defaultBottomSheetModifier = Modifier
    .fillMaxWidth()
    .shadow(11.dp)
    .background(
        Color.White,
        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    )
    .padding(8.dp)
