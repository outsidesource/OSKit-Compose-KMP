package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.unit.IntSize

@Composable
expect fun rememberKmpWindowInfo(): KmpWindowInfo

interface KmpWindowInfo {
    val isWindowFocused: Boolean
    val keyboardModifiers: PointerKeyboardModifiers
    val containerSize: IntSize
}
