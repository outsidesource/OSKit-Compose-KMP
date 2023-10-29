package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    statusBarIconColor: SystemBarIconColor,
    navigationBarColor: Color,
    navigationBarIconColor: SystemBarIconColor,
) {
    // Noop for Desktop
}