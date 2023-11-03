package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

actual typealias PopupPositionProvider = androidx.compose.ui.window.PopupPositionProvider

@OptIn(ExperimentalComposeUiApi::class)
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
) = Popup(
    alignment = alignment,
    offset = offset,
    onDismissRequest = onDismissRequest,
    properties = PopupProperties(
        focusable = focusable,
        dismissOnBackPress = dismissOnBackPress,
        usePlatformInsets = !isFullScreen,
    ),
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = content
)

@OptIn(ExperimentalComposeUiApi::class)
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
) = Popup(
    popupPositionProvider = popupPositionProvider,
    onDismissRequest = onDismissRequest,
    properties = PopupProperties(
        focusable = focusable,
        dismissOnBackPress = dismissOnBackPress,
        usePlatformInsets = !isFullScreen,
    ),
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = content
)