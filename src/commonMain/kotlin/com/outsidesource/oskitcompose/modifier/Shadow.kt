package com.outsidesource.oskitcompose.modifier

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.canvas.kmpBlur

fun Modifier.kmpInnerShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
    drawOverContent: Boolean = false,
) = kmpInnerShadow(
    blur = blur,
    spread = spread,
    brush = SolidColor(color),
    shape = shape,
    offset = offset,
    drawOverContent = drawOverContent,
)

fun Modifier.kmpInnerShadow(
    shadow: KmpShadow,
    drawOverContent: Boolean = false,
) = kmpInnerShadow(
    blur = shadow.blur,
    spread = shadow.spread,
    brush = shadow.brush,
    shape = shadow.shape,
    offset = shadow.offset,
    drawOverContent = drawOverContent,
)

fun Modifier.kmpInnerShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    brush: Brush = SolidColor(Color.Black),
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
    drawOverContent: Boolean = false,
) = (if (!drawOverContent) graphicsLayer { alpha = .99f } else this) // This forces Android to use alpha compositing
    .drawWithContent {
        if (drawOverContent) drawContent()
        drawKmpInnerShadow(blur = blur, spread = spread, brush = brush, shape = shape, offset = offset)
        if (!drawOverContent) drawContent()
    }

fun DrawScope.drawKmpInnerShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) = drawKmpInnerShadow(
    topLeft = topLeft,
    size = size,
    blur = blur,
    spread = spread,
    brush = SolidColor(color),
    shape = shape,
    offset = offset,
)

fun DrawScope.drawKmpInnerShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    shadow: KmpShadow,
) = drawKmpInnerShadow(
    topLeft = topLeft,
    size = size,
    blur = shadow.blur,
    spread = shadow.spread,
    brush = shadow.brush,
    shape = shadow.shape,
    offset = shadow.offset,
)

fun DrawScope.drawKmpInnerShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    blur: Dp,
    spread: Dp = 0.dp,
    brush: Brush = SolidColor(Color.Black),
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) {
    drawIntoCanvas {
        val shadowPaint = Paint().apply { isAntiAlias = true }
        val maskPaint = Paint().apply {
            isAntiAlias = true
            blendMode = BlendMode.DstOut
        }
        brush.applyTo(size = size, p = shadowPaint, alpha = 1f)

        it.withSaveLayer(bounds = Rect(topLeft, size), paint = shadowPaint) {
            translate(
                left = topLeft.x,
                top = topLeft.y,
            ) {
                val fillOutline = shape.createOutline(size, layoutDirection, this)
                it.drawOutline(paint = shadowPaint, outline = fillOutline)
            }

            if (blur.toPx() > 0) maskPaint.kmpBlur(blur.toPx() / 2)

            translate(
                left = topLeft.x + spread.toPx() + offset.x.toPx(),
                top = topLeft.y + spread.toPx() + offset.y.toPx()
            ) {
                val shadowSize = size.copy(width = size.width - (spread.toPx() * 2), height = size.height - (spread.toPx() * 2))
                val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)
                it.drawOutline(paint = maskPaint, outline = shadowOutline)
            }
        }
    }
}

fun Modifier.kmpOuterShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) = kmpOuterShadow(
    blur = blur,
    spread = spread,
    brush = SolidColor(color),
    shape = shape,
    offset = offset,
)

fun Modifier.kmpOuterShadow(
    shadow: KmpShadow,
) = kmpOuterShadow(
    blur = shadow.blur,
    spread = shadow.spread,
    brush = shadow.brush,
    shape = shadow.shape,
    offset = shadow.offset,
)

fun Modifier.kmpOuterShadow(
    blur: Dp,
    spread: Dp = 0.dp,
    brush: Brush = SolidColor(Color.Black),
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) = drawBehind {
    drawKmpOuterShadow(blur = blur, spread = spread, brush = brush, shape = shape, offset = offset)
}

fun DrawScope.drawKmpOuterShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    blur: Dp,
    spread: Dp = 0.dp,
    color: Color = Color.Black,
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) = drawKmpOuterShadow(
    topLeft = topLeft,
    size = size,
    blur = blur,
    spread = spread,
    brush = SolidColor(color),
    shape = shape,
    offset = offset,
)

fun DrawScope.drawKmpOuterShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    shadow: KmpShadow,
) = drawKmpOuterShadow(
    topLeft = topLeft,
    size = size,
    blur = shadow.blur,
    spread = shadow.spread,
    brush = shadow.brush,
    shape = shadow.shape,
    offset = shadow.offset,
)

fun DrawScope.drawKmpOuterShadow(
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    blur: Dp,
    spread: Dp = 0.dp,
    brush: Brush = SolidColor(Color.Black),
    shape: Shape = RectangleShape,
    offset: DpOffset = DpOffset.Zero,
) {
    val paint = Paint().apply { isAntiAlias = true }
    brush.applyTo(size = size, p = paint, alpha = 1f)

    drawIntoCanvas {
        if (blur.toPx() > 0) if (blur.toPx() > 0) paint.kmpBlur(blur.toPx() / 2)
        val shadowSize = size.copy(width = size.width + (spread.toPx() * 2), size.height + (spread.toPx() * 2))
        val outline = shape.createOutline(shadowSize, layoutDirection, this)

        translate(
            left = topLeft.x + -(spread.toPx()) + offset.x.toPx(),
            top = topLeft.y + -(spread.toPx()) + offset.y.toPx(),
        ) {
            it.drawOutline(paint = paint, outline = outline)
        }
    }
}

@Immutable
data class KmpShadow(
    val blur: Dp,
    val spread: Dp = 0.dp,
    val brush: Brush = SolidColor(Color.Black),
    val shape: Shape = RectangleShape,
    val offset: DpOffset = DpOffset.Zero,
) {
    constructor(
        blur: Dp,
        spread: Dp = 0.dp,
        color: Color = Color.Black,
        shape: Shape = RectangleShape,
        offset: DpOffset = DpOffset.Zero,
    ) : this(blur = blur, spread = spread, brush = SolidColor(color), shape = shape, offset = offset)
}