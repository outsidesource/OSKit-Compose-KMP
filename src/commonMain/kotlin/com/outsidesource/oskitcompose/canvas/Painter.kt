package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import okio.*

expect fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap
expect fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter

private val httpClient = HttpClient()

fun kmpBitmapPainter(source: Source) = BitmapPainter(source.buffer().use(::kmpLoadImageBitmap))

fun kmpUrlImagePainter(url: String, density: Density): Painter {
    val buffer = Buffer()

    return try {
        runBlocking {
            val response = httpClient.get(url)
            buffer.write(response.readBytes())
        }

        if (Url(url).encodedPath.endsWith(".svg")) {
            kmpLoadSvgPainter(buffer, density)
        } else {
            BitmapPainter(buffer.use(::kmpLoadImageBitmap))
        }
    } catch (e: Exception) {
        imageLoadErrorPainter(density)
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

internal fun imageLoadErrorPainter(density: Density) = object : Painter() {
    override val intrinsicSize: Size = with(density) { Size(25.dp.toPx(), 25.dp.toPx()) }
    override fun DrawScope.onDraw() {
        val edgeDistance = 8.dp.toPx()

        drawRoundRect(Color(0x20000000), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))

        drawLine(
            color = Color.Black,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
            start = Offset(edgeDistance, edgeDistance),
            end = Offset(size.width - edgeDistance, size.height - edgeDistance)
        )
        drawLine(
            color = Color.Black,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
            start = Offset(edgeDistance, size.height - edgeDistance),
            end = Offset(size.width - edgeDistance, edgeDistance)
        )
    }
}