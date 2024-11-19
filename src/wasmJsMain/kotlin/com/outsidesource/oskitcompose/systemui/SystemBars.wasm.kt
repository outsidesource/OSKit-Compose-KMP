package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    statusBarIconColor: SystemBarIconColor,
    navigationBarColor: Color,
    navigationBarIconColor: SystemBarIconColor,
) {
    // Noop for WASM
}

@Composable
actual fun rememberSystemBarColorController(): ISystemBarColorController {
    return remember {
        object : ISystemBarColorController {
            override fun setStatusBarColor(color: Color) {}
            override fun setStatusBarIconColor(color: SystemBarIconColor) {}
            override fun setNavigationBarColor(color: Color) {}
            override fun setNavigationBarIconColor(color: SystemBarIconColor) {}
        }
    }
}