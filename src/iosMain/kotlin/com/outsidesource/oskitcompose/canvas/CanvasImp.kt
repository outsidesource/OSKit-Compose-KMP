package com.outsidesource.oskitcompose.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import com.outsidesource.oskitcompose.resources.KMPResource
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*

private data class IOSKMPCanvasTypeface(val font: Font) : KMPCanvasTypeface

private data class IOSKMPTextLine(
    override val text: String,
    override val width: Float,
    override val height: Float,
    override val ascent: Float,
    override val descent: Float,
    val textLine: TextLine,
) : KMPTextLine

private val defaultCanvasTypeface = IOSKMPCanvasTypeface(Font(Typeface.makeDefault()))

actual val KMPCanvasTypeface.Companion.Default: KMPCanvasTypeface
    get() = defaultCanvasTypeface

@Composable
actual fun rememberKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface {
    return remember(resource) {
        runBlocking { resolveKmpCanvasTypeface(resource) }
    }
}

actual suspend fun resolveKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface {
    return KMPCanvasTypeface.make(resource)
}

private suspend fun KMPCanvasTypeface.Companion.make(resource: KMPResource): KMPCanvasTypeface {
    val bytes = resource.readBytes()
    return IOSKMPCanvasTypeface(Font(Typeface.makeFromData(Data.makeFromBytes(bytes))))
}

actual interface KMPTextLine {
    actual val text: String
    actual val width: Float
    actual val height: Float
    actual val ascent: Float
    actual val descent: Float

    actual companion object {
        actual fun make(text: String, typeface: KMPCanvasTypeface, size: Float): KMPTextLine {
            val bounds = TextLine.make(text, (typeface as IOSKMPCanvasTypeface).font.makeWithSize(size))

            return IOSKMPTextLine(
                text,
                bounds.width, bounds.height, ascent = bounds.ascent, descent = bounds.descent, bounds
            )
        }
    }
}

actual fun Canvas.drawKmpTextLine(textLine: KMPTextLine, x: Float, y: Float, paint: Paint) {
    nativeCanvas.drawTextLine((textLine as IOSKMPTextLine).textLine, x, y, paint.asFrameworkPaint())
}

actual fun Paint.kmpBlur(radius: Float, mode: KMPBlurMode) {
    val frameworkPaint = asFrameworkPaint()
    frameworkPaint.maskFilter = MaskFilter.makeBlur(
        sigma = radius,
        mode = when (mode) {
            KMPBlurMode.Inner -> FilterBlurMode.INNER
            KMPBlurMode.Outer -> FilterBlurMode.OUTER
            KMPBlurMode.Normal -> FilterBlurMode.NORMAL
            KMPBlurMode.Solid -> FilterBlurMode.SOLID
        },
    )
}
