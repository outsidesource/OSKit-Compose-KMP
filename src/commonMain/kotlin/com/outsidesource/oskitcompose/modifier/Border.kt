package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.borderEnd(color: Color, width: Dp = 1.dp) = padding(end = width)
    .drawBehind {
        val strokeWidth = width.value * density
        val x = size.width + strokeWidth - (strokeWidth / 2)
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = strokeWidth)
    }

fun Modifier.borderStart(color: Color, width: Dp = 1.dp) = padding(start = width)
    .drawBehind {
        val strokeWidth = width.value * density
        val x = -strokeWidth + (strokeWidth / 2)
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = strokeWidth)
    }