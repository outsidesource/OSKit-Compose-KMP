package com.outsidesource.oskitcompose.geometry

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.*

fun Rect.toDpRect(density: Density): DpRect = DpRect(this.topLeft.toDpOffset(density), this.size.toDpSize(density))

val DpRect.topLeft: DpOffset
    get() = DpOffset(left, top)

val DpRect.area: Dp
    get() = (width.value * height.value).dp

