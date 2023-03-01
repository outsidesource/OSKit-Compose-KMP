package com.outsidesource.oskitcompose.canvas

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.outsidesource.oskitcompose.resources.KMPResource

internal data class AndroidKMPTypeface(val typeface: Typeface) : KMPTypeface

actual val KMPTypeface.Companion.Default: KMPTypeface
    get() = AndroidKMPTypeface(Typeface.DEFAULT)

@RequiresApi(Build.VERSION_CODES.O)
fun KMPTypeface.Companion.make(context: Context, resourceId: Int): KMPTypeface {
    return AndroidKMPTypeface(context.resources.getFont(resourceId))
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
actual fun rememberKmpCanvasTypeface(resource: KMPResource): KMPTypeface {
    val context = LocalContext.current
    return remember(resource) { KMPTypeface.make(context, (resource as KMPResource.Android).id) }
}

internal data class AndroidKMPTextLine(
    override val text: String,
    override val width: Float,
    override val height: Float,
    override val ascent: Float,
    override val descent: Float,
    val size: Float,
    val typeface: Typeface,
) : KMPTextLine

actual interface KMPTextLine {
    actual val text: String
    actual val width: Float
    actual val height: Float
    actual val ascent: Float
    actual val descent: Float

    actual companion object {
        actual fun make(text: String, typeface: KMPTypeface, size: Float): KMPTextLine {
            val frameworkPaint = NativePaint()
            frameworkPaint.typeface = (typeface as AndroidKMPTypeface).typeface
            frameworkPaint.textSize = size

            val bounds = Rect()
            val width = frameworkPaint.measureText(text, 0, text.length)
            frameworkPaint.getTextBounds(text, 0, text.length, bounds)

            return AndroidKMPTextLine(
                text, width, bounds.height().toFloat(), bounds.top.toFloat(),
                bounds.bottom.toFloat(), size, typeface.typeface
            )
        }
    }
}

actual fun Canvas.drawKmpTextLine(textLine: KMPTextLine, x: Float, y: Float, paint: Paint) {
    nativeCanvas.drawText(
        textLine.text, x, y,
        paint.asFrameworkPaint().apply {
            textLine as AndroidKMPTextLine
            typeface = textLine.typeface
            textSize = textLine.size
        }
    )
}

actual fun Paint.kmpBlur(radius: Float, mode: KMPBlurMode) {
    val frameworkPaint = asFrameworkPaint()
    frameworkPaint.maskFilter = BlurMaskFilter(
        radius,
        when (mode) {
            KMPBlurMode.Inner -> BlurMaskFilter.Blur.INNER
            KMPBlurMode.Outer -> BlurMaskFilter.Blur.OUTER
            KMPBlurMode.Normal -> BlurMaskFilter.Blur.NORMAL
            KMPBlurMode.Solid -> BlurMaskFilter.Blur.SOLID
        },
    )
}