package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    // Noop for Desktop
}

@Composable
actual fun StatusBarIconColorEffect(
    useDarkIcons: Boolean,
) {
    // Noop for Desktop
}

@Composable
actual fun SystemBarIconColorEffect(
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean,
) {
    // Noop for Desktop
}