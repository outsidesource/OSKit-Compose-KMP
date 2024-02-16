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

enum class PopupShapeCaretPosition {
    Top,
    Bottom,
    Start,
    End,
}

class PopupShape(
    private val cornerRadius: Dp = 8.dp,
    private val caretThickness: Dp = 20.dp,
    private val caretPointHeight: Dp = 12.dp,
    private val caretOffset: Float = .5f,
    private val caretPosition: PopupShapeCaretPosition = PopupShapeCaretPosition.Bottom,
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

            val offsetDimensionTotalSize = when (caretPosition) {
                PopupShapeCaretPosition.Bottom,
                PopupShapeCaretPosition.Top -> size.width - caretThickness.toPx() - cornerRadius.toPx() * 2
                else -> size.height - caretThickness.toPx() - cornerRadius.toPx() * 2
            }

            val offsetInitial = (caretThickness.toPx() / 2) + cornerRadius.toPx()

            when (caretPosition) {
                PopupShapeCaretPosition.Bottom -> {
                    path.moveTo((offsetDimensionTotalSize * caretOffset) + offsetInitial - (caretThickness.toPx() / 2), size.height)
                    path.lineTo((offsetDimensionTotalSize * caretOffset) + offsetInitial, size.height + caretPointHeight.toPx())
                    path.lineTo((offsetDimensionTotalSize * caretOffset) + offsetInitial + (caretThickness.toPx() / 2), size.height)
                }
                PopupShapeCaretPosition.Top -> {
                    path.moveTo((offsetDimensionTotalSize * caretOffset) + offsetInitial - (caretThickness.toPx() / 2), 0f)
                    path.lineTo((offsetDimensionTotalSize * caretOffset) + offsetInitial, -caretPointHeight.toPx())
                    path.lineTo((offsetDimensionTotalSize * caretOffset) + offsetInitial + (caretThickness.toPx() / 2), 0f)
                }
                PopupShapeCaretPosition.Start -> {
                    path.moveTo(0f, (offsetDimensionTotalSize * caretOffset) + offsetInitial - (caretThickness.toPx() / 2))
                    path.lineTo(-caretPointHeight.toPx(), (offsetDimensionTotalSize * caretOffset) + offsetInitial)
                    path.lineTo(0f, (offsetDimensionTotalSize * caretOffset) + offsetInitial + (caretThickness.toPx() / 2))
                }
                PopupShapeCaretPosition.End -> {
                    path.moveTo(size.width, (offsetDimensionTotalSize * caretOffset) + offsetInitial - (caretThickness.toPx() / 2))
                    path.lineTo(size.width + caretPointHeight.toPx(), (offsetDimensionTotalSize * caretOffset) + offsetInitial)
                    path.lineTo(size.width, (offsetDimensionTotalSize * caretOffset) + offsetInitial + (caretThickness.toPx() / 2))
                }
            }

            path.close()

            return Outline.Generic(path)
        }
    }
}