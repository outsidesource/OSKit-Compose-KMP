package com.outsidesource.oskitcompose.geometry

import androidx.compose.ui.unit.Dp

operator fun Dp.rem(other: Float): Dp = Dp(value = value % other)