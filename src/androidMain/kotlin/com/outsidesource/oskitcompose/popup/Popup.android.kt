package com.outsidesource.oskitcompose.popup

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

actual typealias PopupPositionProvider = androidx.compose.ui.window.PopupPositionProvider

@Composable
actual fun KmpPopup(
    alignment: Alignment,
    offset: IntOffset,
    dismissOnBackPress: Boolean,
    onDismissRequest: (() -> Unit)?,
    focusable: Boolean,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    isFullScreen: Boolean,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    if (!isFullScreen) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = focusable,
                excludeFromSystemGesture = false,
                dismissOnBackPress = dismissOnBackPress,
            ),
            content = { LocalLayoutDirectionWrapper(layoutDirection, content) },
        )
    } else {
        AndroidFullScreenPopup(
            onDismissRequest = onDismissRequest,
            properties = AndroidFullScreenPopupProperties(
                focusable = focusable,
                dismissOnBackPress = dismissOnBackPress,
            ),
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = { LocalLayoutDirectionWrapper(layoutDirection, content) },
        )
    }
}

@Composable
actual fun KmpPopup(
    popupPositionProvider: PopupPositionProvider,
    dismissOnBackPress: Boolean,
    onDismissRequest: (() -> Unit)?,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    focusable: Boolean,
    isFullScreen: Boolean,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val focusRequester = remember { FocusRequester() }
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    if (!isFullScreen) {
        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = focusable,
                excludeFromSystemGesture = false,
                dismissOnBackPress = false,
            ),
            content = {
                Box(
                    modifier = Modifier
                        .onKeyEvent {
                            val consumed = onKeyEvent(it)
                            if (consumed) return@onKeyEvent true

                            if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                                if (backPressedDispatcherOwner?.onBackPressedDispatcher?.hasEnabledCallbacks() == true) {
                                    backPressedDispatcherOwner.onBackPressedDispatcher.onBackPressed()
                                    return@onKeyEvent true
                                } else {
                                    if (!dismissOnBackPress) return@onKeyEvent false

                                    onDismissRequest?.invoke()
                                    return@onKeyEvent true
                                }
                            }

                            false
                        }
                        .onPreviewKeyEvent(onPreviewKeyEvent)
                        .focusRequester(focusRequester)
                        .focusable()
                ) {
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    LocalLayoutDirectionWrapper(layoutDirection, content)
                }
            },
        )
    } else {
        AndroidFullScreenPopup(
            onDismissRequest = onDismissRequest,
            properties = AndroidFullScreenPopupProperties(
                focusable = focusable,
                dismissOnBackPress = dismissOnBackPress,
            ),
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = { LocalLayoutDirectionWrapper(layoutDirection, content) },
        )
    }
}