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

    Popup(
        properties = PopupProperties(
            focusable = focusable,
            dismissOnBackPress = dismissOnBackPress,
        ),
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismissRequest,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        content = { LocalLayoutDirectionWrapper(layoutDirection, content) },
    )
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

    Popup(
        popupPositionProvider = popupPositionProvider,
        properties = PopupProperties(
            focusable = focusable,
            dismissOnBackPress = dismissOnBackPress,
        ),
        onDismissRequest = onDismissRequest,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        content = { LocalLayoutDirectionWrapper(layoutDirection, content) }
    )
}