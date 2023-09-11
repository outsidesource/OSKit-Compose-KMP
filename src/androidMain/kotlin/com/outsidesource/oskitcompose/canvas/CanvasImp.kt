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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.io.File

private data class AndroidKMPCanvasTypeface(val typeface: Typeface) : KMPCanvasTypeface

private data class AndroidKMPTextLine(
    override val text: String,
    override val width: Float,
    override val height: Float,
    override val ascent: Float,
    override val descent: Float,
    val size: Float,
    val typeface: Typeface,
) : KMPTextLine

actual val KMPCanvasTypeface.Companion.Default: KMPCanvasTypeface
    get() = AndroidKMPCanvasTypeface(Typeface.DEFAULT)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
actual fun rememberKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface {
    val context = LocalContext.current

    return remember(resource) {
        if (resource.resourceId != null) {
            KMPCanvasTypeface.make(context, resource.resourceId)
        } else if (resource.path != null) {
            runBlocking { KMPCanvasTypeface.make(resource.path) }
        } else {
            // This cannot happen due to the KMPResource constructors
            KMPCanvasTypeface.Default
        }
    }
}

actual suspend fun resolveKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface {
    return if (resource.resourceId != null) {
        KMPCanvasTypeface.Default
    } else if (resource.path != null) {
        KMPCanvasTypeface.make(resource.path)
    } else {
        // This cannot happen due to the KMPResource constructors
        KMPCanvasTypeface.Default
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun KMPCanvasTypeface.Companion.make(context: Context, resourceId: Int): KMPCanvasTypeface {
    return AndroidKMPCanvasTypeface(context.resources.getFont(resourceId))
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun KMPCanvasTypeface.Companion.make(path: String): KMPCanvasTypeface = withContext(Dispatchers.IO) {
    val name = path.toPath().name
    val file = File.createTempFile(name, null)
    val os = file.outputStream()
    val bytes = runBlocking { resource(path).readBytes() }

    os.write(bytes)
    os.flush()
    os.close()

    return@withContext AndroidKMPCanvasTypeface(Typeface.createFromFile(file))
}

actual interface KMPTextLine {
    actual val text: String
    actual val width: Float
    actual val height: Float
    actual val ascent: Float
    actual val descent: Float

    actual companion object {
        actual fun make(text: String, typeface: KMPCanvasTypeface, size: Float): KMPTextLine {
            val frameworkPaint = NativePaint()
            frameworkPaint.typeface = (typeface as AndroidKMPCanvasTypeface).typeface
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