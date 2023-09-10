package com.outsidesource.oskitcompose.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import com.outsidesource.oskitcompose.resources.KMPResource

expect interface KMPTextLine {
    val text: String
    val width: Float
    val height: Float
    val ascent: Float
    val descent: Float

    companion object {
        fun make(text: String, typeface: KMPCanvasTypeface, size: Float): KMPTextLine
    }
}

interface KMPCanvasTypeface {
    companion object
}

@Composable
expect fun rememberKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface

/**
 * Resolves a KMPCanvasTypeface from a KMPResource.
 * Note: Android KMPResources that use resource Ids (Int) will return the default typeface
 */
expect suspend fun resolveKmpCanvasTypeface(resource: KMPResource): KMPCanvasTypeface

expect val KMPCanvasTypeface.Companion.Default: KMPCanvasTypeface

expect fun Canvas.drawKmpTextLine(textLine: KMPTextLine, x: Float, y: Float, paint: Paint)

expect fun Paint.kmpBlur(radius: Float, mode: KMPBlurMode = KMPBlurMode.Normal)

enum class KMPBlurMode {
    Normal,
    Solid,
    Inner,
    Outer,
}
