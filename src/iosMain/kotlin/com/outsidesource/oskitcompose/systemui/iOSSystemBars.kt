package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.LocalUIViewController
import com.outsidesource.oskitcompose.uikit.OSUIViewControllerWrapper
import platform.UIKit.UIColor

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    statusBarIconColor: SystemBarIconColor,
    navigationBarColor: Color,
    navigationBarIconColor: SystemBarIconColor,
) {
    val vc = LocalUIViewController.current.parentViewController

    DisposableEffect(statusBarColor, statusBarIconColor) {
        if (vc !is OSUIViewControllerWrapper) return@DisposableEffect onDispose {  }

        vc.setStatusBarBackground(UIColor(
            red = statusBarColor.red.toDouble(),
            green = statusBarColor.green.toDouble(),
            blue = statusBarColor.blue.toDouble(),
            alpha = statusBarColor.alpha.toDouble(),
        ))

        vc.setNavigationBarBackground(UIColor(
            red = navigationBarColor.red.toDouble(),
            green = navigationBarColor.green.toDouble(),
            blue = navigationBarColor.blue.toDouble(),
            alpha = navigationBarColor.alpha.toDouble(),
        ))

        when (statusBarIconColor) {
            SystemBarIconColor.Unspecified -> {}
            SystemBarIconColor.Dark -> vc.setStatusBarIconColor(true)
            SystemBarIconColor.Light -> vc.setStatusBarIconColor(false)
        }

        onDispose {  }
    }
}