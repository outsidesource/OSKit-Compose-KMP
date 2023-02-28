package com.outsidesource.oskitcompose.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

actual fun Modifier.innerShadow(
    color: Color,
    shape: Shape,
    spread: Dp,
    blur: Dp,
    offset: DpOffset,
) = drawWithContent {
    drawContent()

    val rect = Rect(Offset.Zero, size)
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()

    drawIntoCanvas {
        paint.color = color
        paint.isAntiAlias = true

        it.saveLayer(rect, paint)

        val fillOutline = shape.createOutline(size, layoutDirection, this)
        it.drawOutline(paint = paint, outline = fillOutline)

        frameworkPaint.blendMode = BlendMode.DST_OUT

        if (blur.toPx() > 0) frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blur.toPx() / 2)

        translate(
            left = spread.toPx() + offset.x.toPx(),
            top = spread.toPx() + offset.y.toPx()
        ) {
            val shadowSize = size.copy(width = size.width - (spread.toPx() * 2), size.height - (spread.toPx() * 2))
            val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)
            it.drawOutline(paint = paint, outline = shadowOutline)
        }
    }
}

actual fun Modifier.outerShadow(
    color: Color,
    shape: Shape,
    spread: Dp,
    blur: Dp,
    offset: DpOffset,
) = drawBehind {
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()

    drawIntoCanvas {
        paint.color = color
        paint.isAntiAlias = true

        if (blur.toPx() > 0) frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blur.toPx() / 2)
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