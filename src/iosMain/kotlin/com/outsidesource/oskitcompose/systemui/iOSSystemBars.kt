package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
    val sbc = rememberSystemBarColorController()

    DisposableEffect(statusBarColor, statusBarIconColor) {
        sbc.setStatusBarColor(statusBarColor)
        sbc.setNavigationBarColor(navigationBarColor)
        sbc.setStatusBarIconColor(statusBarIconColor)

        onDispose {  }
    }
}

@Composable
actual fun rememberSystemBarColorController(): ISystemBarColorController {
    val vc = LocalUIViewController.current.parentViewController

    return remember {
        object : ISystemBarColorController {
            override fun setStatusBarColor(color: Color) {
                if (vc !is OSUIViewControllerWrapper) return
                vc.setStatusBarBackground(UIColor(
                    red = color.red.toDouble(),
                    green = color.green.toDouble(),
                    blue = color.blue.toDouble(),
                    alpha = color.alpha.toDouble(),
                ))
            }

            override fun setStatusBarIconColor(color: SystemBarIconColor) {
                if (vc !is OSUIViewControllerWrapper) return
                when (color) {
                    SystemBarIconColor.Unspecified -> {}
                    SystemBarIconColor.Dark -> vc.setStatusBarIconColor(true)
                    SystemBarIconColor.Light -> vc.setStatusBarIconColor(false)
                }
            }

            override fun setNavigationBarColor(color: Color) {
                if (vc !is OSUIViewControllerWrapper) return
                vc.setNavigationBarBackground(color)
            }

            override fun setNavigationBarIconColor(color: SystemBarIconColor) {}
        }
    }
}