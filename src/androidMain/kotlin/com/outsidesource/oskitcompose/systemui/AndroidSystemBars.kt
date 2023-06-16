package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(statusBarColor, navigationBarColor) {
        systemUiController.setStatusBarColor(statusBarColor)
        systemUiController.setNavigationBarColor(navigationBarColor)
        systemUiController.navigationBarDarkContentEnabled = true
        systemUiController.isNavigationBarContrastEnforced = false
        onDispose { }
    }
}

@Composable
actual fun StatusBarIconColorEffect(
    useDarkIcons: Boolean,
) {
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(useDarkIcons) {
        systemUiController.statusBarDarkContentEnabled = useDarkIcons
    }
}