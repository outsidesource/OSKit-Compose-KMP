package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberKMPWindowInfo(): KMPWindowInfo {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) {
        object : KMPWindowInfo {
            override val isWindowFocused: Boolean
                get() = windowInfo.isWindowFocused
            override val keyboardModifiers: PointerKeyboardModifiers
                get() = windowInfo.keyboardModifiers
            override val containerSize: IntSize
                get() = windowInfo.containerSize
        }
    }
}