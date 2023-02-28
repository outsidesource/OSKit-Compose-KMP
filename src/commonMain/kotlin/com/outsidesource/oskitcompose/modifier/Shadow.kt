package com.outsidesource.oskitcompose.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

expect fun Modifier.innerShadow(
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    spread: Dp = 0.dp,
    blur: Dp = 0.dp,
    offset: DpOffset = DpOffset.Zero,
): Modifier

expect fun Modifier.outerShadow(
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    spread: Dp = 0.dp,
    blur: Dp = 0.dp,
    offset: DpOffset = DpOffset.Zero,
): Modifier