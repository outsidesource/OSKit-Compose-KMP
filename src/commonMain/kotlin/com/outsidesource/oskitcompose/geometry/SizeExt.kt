package com.outsidesource.oskitcompose.geometry

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

fun Size.toDpSize(density: Density): DpSize {
    val adjustedSize = this / density.density
    return DpSize(adjustedSize.width.dp, adjustedSize.height.dp)
}