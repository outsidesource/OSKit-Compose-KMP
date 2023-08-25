package com.outsidesource.oskitcompose.geometry

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class PopupShape(
    private val cornerRadius: Dp = 8.dp,
    private val caretWidth: Dp = 20.dp,
    private val caretHeight: Dp = 12.dp,
    private val caretOffset: Float = .5f,
): Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        with(density) {
            val path = Path()

            path.addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            )

            val offsetTotalWidth = size.width - caretWidth.toPx() - cornerRadius.toPx() * 2
            val offsetInitialX = (caretWidth.toPx() / 2) + cornerRadius.toPx()
            path.moveTo((offsetTotalWidth * caretOffset) + offsetInitialX - (caretWidth.toPx() / 2), size.height)
            path.lineTo((offsetTotalWidth * caretOffset) + offsetInitialX, size.height + caretHeight.toPx())
            path.lineTo((offsetTotalWidth * caretOffset) + offsetInitialX + (caretWidth.toPx() / 2), size.height)
            path.close()

            return Outline.Generic(path)
        }
    }
}