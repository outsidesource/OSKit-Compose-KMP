package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SystemBarColorEffect(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
)

@Composable
expect fun StatusBarIconColorEffect(
    useDarkIcons: Boolean = true,
)

@Composable
expect fun SystemBarIconColorEffect(
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean,
)