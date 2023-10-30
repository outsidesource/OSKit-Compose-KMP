package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * [SystemBarColorEffect] Configures the system toolbars for Android and iOS. On desktop this effect is a no-op.
 *
 * iOS Notes: In order to use [SystemBarColorEffect] in iOS, replace [ComposeUIViewController]
 * with [OSComposeUIViewController].
 * [navigationBarIconColor] is a no-op in iOS.
 */
@Composable
expect fun SystemBarColorEffect(
    statusBarColor: Color = Color.Transparent,
    statusBarIconColor: SystemBarIconColor = SystemBarIconColor.Unspecified,
    navigationBarColor: Color = Color.Transparent,
    navigationBarIconColor: SystemBarIconColor = SystemBarIconColor.Unspecified,
)


interface ISystemBarColorController {
    fun setStatusBarColor(color: Color)
    fun setStatusBarIconColor(color: SystemBarIconColor)
    fun setNavigationBarColor(color: Color)
    fun setNavigationBarIconColor(color: SystemBarIconColor)
}

@Composable
expect fun rememberSystemBarColorController(): ISystemBarColorController

/**
 * [SystemBarIconColor] defines to change system icons to dark or light. Using [Unspecified] will not change
 * the current setting.
 */
enum class SystemBarIconColor {
    Unspecified,
    Dark,
    Light,
}