package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

actual typealias PopupPositionProvider = androidx.compose.ui.window.PopupPositionProvider

@Composable
actual fun Popup(
    alignment: Alignment,
    offset: IntOffset,
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
    ),
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = content
)

@Composable
actual fun Popup(
    popupPositionProvider: PopupPositionProvider,
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
    ),
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = content
)