package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
actual fun rememberKmpWindowInfo(): KmpWindowInfo {
    val windowInfo = LocalWindowInfo.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    return remember(windowInfo, configuration, density) {
        object : KmpWindowInfo {
            override val isWindowFocused: Boolean
                get() = windowInfo.isWindowFocused
            override val keyboardModifiers: PointerKeyboardModifiers
                get() = windowInfo.keyboardModifiers
            override val containerSize: IntSize
                get() = with(density) {
                    IntSize(
                        width = configuration.screenWidthDp.dp.toPx().roundToInt(),
                        height = configuration.screenHeightDp.dp.toPx().roundToInt()
                    )
                }
        }
    }
}