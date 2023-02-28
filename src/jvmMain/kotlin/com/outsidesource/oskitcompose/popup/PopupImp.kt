package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.IntOffset

actual typealias PopupPositionProvider = androidx.compose.ui.window.PopupPositionProvider

@Composable
actual fun Popup(
    alignment: Alignment,
    offset: IntOffset,
    onDismissRequest: (() -> Unit)?,
    focusable: Boolean,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    content: @Composable () -> Unit,
) = androidx.compose.ui.window.Popup(
    alignment = alignment,
    offset = offset,
    onDismissRequest = onDismissRequest,
    focusable = focusable,
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
    content: @Composable () -> Unit,
) = androidx.compose.ui.window.Popup(
    popupPositionProvider = popupPositionProvider,
    onDismissRequest = onDismissRequest,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    focusable = focusable,
    content = content,
)
