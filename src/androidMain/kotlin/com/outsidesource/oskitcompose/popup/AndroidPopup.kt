package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

actual typealias PopupPositionProvider = androidx.compose.ui.window.PopupPositionProvider

@Composable
actual fun KMPPopup(
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
actual fun KMPPopup(
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

    if (!isFullScreen) {
        Popup(
            popupPositionProvider = popupPositionProvider,
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