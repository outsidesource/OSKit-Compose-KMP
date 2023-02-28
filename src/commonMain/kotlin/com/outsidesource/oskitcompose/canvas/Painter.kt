package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.outsidesource.oskitcompose.resources.KMPResource
import java.io.InputStream
import java.net.URL

expect fun rememberKmpPainterResource(resource: KMPResource): Painter
expect fun kmpLoadImageBitmap(input: InputStream): ImageBitmap
expect fun kmpLoadSvgPainter(input: InputStream, density: Density): Painter

fun kmpBitmapPainter(input: InputStream) = BitmapPainter(input.use(::kmpLoadImageBitmap))

fun kmpUrlImagePainter(url: String, density: Density): Painter {
    val inputStream = URL(url).openStream().buffered()

    return if (url.contains(".svg")) {
        kmpLoadSvgPainter(inputStream, density)
    } else {
        return BitmapPainter(inputStream.use(::kmpLoadImageBitmap))
    }
}

fun Painter.asBitmap(density: Density, size: Size): ImageBitmap {
    val painter = this@asBitmap
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)

    CanvasDrawScope().draw(density, LayoutDirection.Ltr, canvas, size) {
        with(painter) {
            draw(size)
        }
    }

    return bitmap
}