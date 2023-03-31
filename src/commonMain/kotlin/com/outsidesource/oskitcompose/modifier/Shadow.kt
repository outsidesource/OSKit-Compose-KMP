package com.outsidesource.oskitcompose.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.canvas.kmpBlur

fun Modifier.innerShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
    drawOverContent: Boolean = false,
) = (if (!drawOverContent) graphicsLayer { alpha = .99f } else this) // This forces Android to use alpha compositing
    .drawWithContent {
        if (drawOverContent) drawContent()

        val rect = Rect(Offset.Zero, size)
        val paint = Paint()

        drawIntoCanvas {
            paint.color = color
            paint.isAntiAlias = true

            if (drawOverContent) it.saveLayer(rect, paint)

            val fillOutline = shape.createOutline(size, layoutDirection, this)
            it.drawOutline(paint = paint, outline = fillOutline)

            paint.blendMode = BlendMode.DstOut

            if (blur.toPx() > 0) paint.kmpBlur(blur.toPx() / 2)

            translate(
                left = spread.toPx() + offset.x.toPx(),
                top = spread.toPx() + offset.y.toPx()
            ) {
                val shadowSize = size.copy(width = size.width - (spread.toPx() * 2), size.height - (spread.toPx() * 2))
                val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)
                it.drawOutline(paint = paint, outline = shadowOutline)
            }
        }

        if (!drawOverContent) drawContent()
    }

fun Modifier.outerShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) = drawBehind {
    val paint = Paint()

    drawIntoCanvas {
        paint.color = color
        paint.isAntiAlias = true

        if (blur.toPx() > 0) if (blur.toPx() > 0) paint.kmpBlur(blur.toPx() / 2)
        val shadowSize = size.copy(width = size.width + (spread.toPx() * 2), size.height + (spread.toPx() * 2))
        val outline = shape.createOutline(shadowSize, layoutDirection, this)

        translate(
            left = -(spread.toPx()) + offset.x.toPx(),
            top = -(spread.toPx()) + offset.y.toPx()
        ) {
            it.drawOutline(paint = paint, outline = outline)
        }
    }
}