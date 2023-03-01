package com.outsidesource.oskitcompose.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.useResource
import com.outsidesource.oskitcompose.resources.KMPResource
import org.jetbrains.skia.*

internal data class DesktopKMPTypeface(val font: Font) : KMPTypeface

private val defaultCanvasTypeface = DesktopKMPTypeface(Font(Typeface.makeDefault()))

actual val KMPTypeface.Companion.Default: KMPTypeface
    get() = defaultCanvasTypeface

fun KMPTypeface.Companion.make(path: String): KMPTypeface {
    val bytes = useResource(path) { it.readAllBytes() }
    return DesktopKMPTypeface(Font(Typeface.makeFromData(Data.makeFromBytes(bytes))))
}

@Composable
actual fun rememberKmpCanvasTypeface(resource: KMPResource): KMPTypeface {
    return remember(resource) { KMPTypeface.make((resource as KMPResource.Desktop).path) }
}

internal data class DesktopKMPTextLine(
    override val text: String,
    override val width: Float,
    override val height: Float,
    override val ascent: Float,
    override val descent: Float,
    val textLine: TextLine,
) : KMPTextLine

actual interface KMPTextLine {
    actual val text: String
    actual val width: Float
    actual val height: Float
    actual val ascent: Float
    actual val descent: Float

    actual companion object {
        actual fun make(text: String, typeface: KMPTypeface, size: Float): KMPTextLine {
            val bounds = TextLine.make(text, (typeface as DesktopKMPTypeface).font.makeWithSize(size))

            return DesktopKMPTextLine(
                text,
                bounds.width, bounds.height, ascent = bounds.ascent, descent = bounds.descent, bounds
            )
        }
    }
}

actual fun Canvas.drawKmpTextLine(textLine: KMPTextLine, x: Float, y: Float, paint: Paint) {
    nativeCanvas.drawTextLine((textLine as DesktopKMPTextLine).textLine, x, y, paint.asFrameworkPaint())
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
