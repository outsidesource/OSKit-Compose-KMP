package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.uikit.IOSInsets
import androidx.compose.ui.uikit.LocalSafeAreaState
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
    if (!isFullScreen) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = focusable,
                dismissOnBackPress = dismissOnBackPress,
            ),
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = content
        )
    } else {
        FullScreenPopup(
            dismissOnBackPress = dismissOnBackPress,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            focusable = focusable,
            content = content,
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
    if (!isFullScreen) {
        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = focusable,
                dismissOnBackPress = dismissOnBackPress,
            ),
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            content = content
        )
    } else {
        FullScreenPopup(
            dismissOnBackPress = dismissOnBackPress,
            onDismissRequest = onDismissRequest,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
            focusable = focusable,
            content = content,
        )
    }
}

@OptIn(InternalComposeApi::class)
@Composable
private fun FullScreenPopup(
    dismissOnBackPress: Boolean,
    onDismissRequest: (() -> Unit)?,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    focusable: Boolean,
    content: @Composable () -> Unit,
) {
    val currentSafeArea = LocalSafeAreaState.current

    CompositionLocalProvider(LocalSafeAreaState provides remember { mutableStateOf(IOSInsets()) }) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = focusable,
                dismissOnBackPress = dismissOnBackPress,
            ),
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
        ) {
            CompositionLocalProvider(LocalSafeAreaState provides currentSafeArea) {
                content()
            }
        }
    }
}