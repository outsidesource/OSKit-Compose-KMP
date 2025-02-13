package com.outsidesource.oskitcompose.systemui

import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.outsidesource.oskitcompose.context.findWindow

@Composable
actual fun SystemBarColorEffect(
    statusBarColor: Color,
    statusBarIconColor: SystemBarIconColor,
    navigationBarColor: Color,
    navigationBarIconColor: SystemBarIconColor,
) {
    val sbc = rememberSystemBarColorController()

    DisposableEffect(statusBarColor, statusBarIconColor, navigationBarColor, navigationBarIconColor) {
        sbc.setStatusBarColor(statusBarColor)
        sbc.setNavigationBarColor(navigationBarColor)
        sbc.setStatusBarIconColor(statusBarIconColor)
        sbc.setNavigationBarIconColor(navigationBarIconColor)
        onDispose {  }
    }
}

@Composable
actual fun rememberSystemBarColorController(): ISystemBarColorController {
    val systemUiController = rememberSystemUiController()

    return remember {
        object : ISystemBarColorController {
            override fun setStatusBarColor(color: Color) {
                systemUiController.setStatusBarColor(color)
            }

            override fun setStatusBarIconColor(color: SystemBarIconColor) {
                when (color) {
                    SystemBarIconColor.Unspecified -> {}
                    SystemBarIconColor.Dark -> systemUiController.statusBarDarkContentEnabled = true
                    SystemBarIconColor.Light -> systemUiController.statusBarDarkContentEnabled = false
                }
            }

            override fun setNavigationBarColor(color: Color) {
                systemUiController.setNavigationBarColor(color)
                systemUiController.isNavigationBarContrastEnforced = true
            }

            override fun setNavigationBarIconColor(color: SystemBarIconColor) {
                when (color) {
                    SystemBarIconColor.Unspecified -> {}
                    SystemBarIconColor.Dark -> systemUiController.navigationBarDarkContentEnabled = true
                    SystemBarIconColor.Light -> systemUiController.navigationBarDarkContentEnabled = false
                }
            }
        }
    }
}

/**
 * Taken from Accompanist SystemUI as it was deprecated as of 10-2023
 */
@Composable
private fun rememberSystemUiController(
    window: Window? = findWindow(),
): AndroidSystemUiController {
    val view = LocalView.current
    return remember(view, window) { AndroidSystemUiController(view, window) }
}

@Composable
private fun findWindow(): Window? =
    (LocalView.current.parent as? DialogWindowProvider)?.window
        ?: LocalView.current.context.findWindow()

internal class AndroidSystemUiController(
    private val view: View,
    private val window: Window?
) {
    private val windowInsetsController = window?.let {
        WindowCompat.getInsetsController(it, view)
    }


    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        statusBarDarkContentEnabled = darkIcons

        window?.statusBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightStatusBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }
            else -> color
        }.toArgb()
    }

    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        navigationBarDarkContentEnabled = darkIcons
        isNavigationBarContrastEnforced = navigationBarContrastEnforced

        window?.navigationBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightNavigationBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }
            else -> color
        }.toArgb()
    }

    var systemBarsBehavior: Int
        get() = windowInsetsController?.systemBarsBehavior ?: 0
        set(value) {
            windowInsetsController?.systemBarsBehavior = value
        }

    var isStatusBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    var isNavigationBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    var statusBarDarkContentEnabled: Boolean
        get() = windowInsetsController?.isAppearanceLightStatusBars == true
        set(value) {
            windowInsetsController?.isAppearanceLightStatusBars = value
        }

    var navigationBarDarkContentEnabled: Boolean
        get() = windowInsetsController?.isAppearanceLightNavigationBars == true
        set(value) {
            windowInsetsController?.isAppearanceLightNavigationBars = value
        }

    var isNavigationBarContrastEnforced: Boolean
        get() = Build.VERSION.SDK_INT >= 29 && window?.isNavigationBarContrastEnforced == true
        set(value) {
            if (Build.VERSION.SDK_INT >= 29) {
                window?.isNavigationBarContrastEnforced = value
            }
        }
}

private val BlackScrim = Color(0f, 0f, 0f, 0.3f) // 30% opaque black
private val BlackScrimmed: (Color) -> Color = { original ->
    BlackScrim.compositeOver(original)
}

