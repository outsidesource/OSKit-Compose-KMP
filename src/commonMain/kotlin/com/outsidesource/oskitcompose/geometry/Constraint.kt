package com.outsidesource.oskitcompose.geometry

import androidx.compose.ui.unit.Dp

data class Constraint(
    val min: Dp = Dp.Unspecified,
    val max: Dp = Dp.Unspecified
)