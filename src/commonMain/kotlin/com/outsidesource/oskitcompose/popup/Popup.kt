package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

@Immutable
expect interface PopupPositionProvider {
    fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset
}

@Composable
expect fun Popup(
    alignment: Alignment = Alignment.Center,
    offset: IntOffset = IntOffset.Zero,
    onDismissRequest: (() -> Unit)? = null,
    focusable: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable () -> Unit,
)

@Composable
expect fun Popup(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: (() -> Unit)? = null,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusable: Boolean = false,
    content: @Composable () -> Unit,
)
