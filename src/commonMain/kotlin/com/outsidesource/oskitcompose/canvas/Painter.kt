package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import java.io.InputStream
import java.net.URL

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