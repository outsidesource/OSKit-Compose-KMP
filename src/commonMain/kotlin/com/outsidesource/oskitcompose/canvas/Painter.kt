package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import okio.*

expect fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap
expect fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter

private val httpClient = HttpClient()

fun kmpBitmapPainter(source: Source) = BitmapPainter(source.buffer().use(::kmpLoadImageBitmap))

fun kmpUrlImagePainter(url: String, density: Density): Painter {
    val buffer = Buffer()

    runBlocking {
        val response = httpClient.get(url)
        buffer.write(response.readBytes())
    }

    return if (url.endsWith(".svg")) {
        kmpLoadSvgPainter(buffer, density)
    } else {
        return BitmapPainter(buffer.use(::kmpLoadImageBitmap))
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