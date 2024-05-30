package com.outsidesource.oskitcompose.popup

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalLayoutDirection
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

/**
 * Creates a customizable Popup with a given alignment
 *
 * @param alignment The alignment relative to the parent.
 * @param dismissOnBackPress Calls onDismissRequest when the back button or escape button is pressed
 * @param offset An offset from the original aligned position of the popup. Offset respects the
 * Ltr/Rtl context, thus in Ltr it will be added to the original aligned position and in Rtl it
 * will be subtracted from it.
 * @param onDismissRequest Executes when the user clicks outside the popup.
 * @param focusable Whether the popup is focusable. When true, the popup will receive IME
 * events and key presses, such as when the back button is pressed.
 * @param onPreviewKeyEvent Handles the onPreviewKey event
 * @param onKeyEvent Handles the onKeyEvent. [onKeyEvent] allows consuming key events before reaching any BackHandlers.
 * @param isFullScreen Utilized in Android and iOS. Specifies whether to draw behind the system bars or not. Setting
 * [isFullScreen] to true will ignore [alignment] and [offset] parameters
 * @param content The content to be displayed inside the popup.
 */
@Composable
expect fun KMPPopup(
    alignment: Alignment = Alignment.Center,
    offset: IntOffset = IntOffset.Zero,
    dismissOnBackPress: Boolean = false,
    onDismissRequest: (() -> Unit)? = null,
    focusable: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    isFullScreen: Boolean = true,
    content: @Composable () -> Unit,
)

/**
 * Creates a customizable Popup with a given position
 *
 * @param popupPositionProvider Calculates the position of a popup on screen.
 * @param dismissOnBackPress Calls onDismissRequest when the back button or escape button is pressed
 * @param onDismissRequest Executes when the user clicks outside the popup.
 * @param focusable Whether the popup is focusable. When true, the popup will receive IME
 * events and key presses, such as when the back button is pressed.
 * @param onPreviewKeyEvent Handles the onPreviewKey event
 * @param onKeyEvent Handles the onKeyEvent
 * @param isFullScreen Utilized in Android and iOS. Specifies whether to draw behind the system bars or not. Setting
 * [isFullScreen] to true will ignore [popupPositionProvider]
 * @param content The content to be displayed inside the popup.
 */
@Composable
expect fun KMPPopup(
    popupPositionProvider: PopupPositionProvider,
    dismissOnBackPress: Boolean = false,
    onDismissRequest: (() -> Unit)? = null,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    focusable: Boolean = false,
    isFullScreen: Boolean = true,
    content: @Composable () -> Unit,
)

@Composable
internal fun LocalLayoutDirectionWrapper(
    layoutDirection: LayoutDirection,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        content()
    }
}