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
        fun make(text: String, typeface: KMPTypeface, size: Float): KMPTextLine
    }
}

interface KMPTypeface {
    companion object
}

@Composable
expect fun rememberKmpCanvasTypeface(resource: KMPResource): KMPTypeface

expect val KMPTypeface.Companion.Default: KMPTypeface

expect fun Canvas.drawKmpTextLine(textLine: KMPTextLine, x: Float, y: Float, paint: Paint)

expect fun Paint.kmpBlur(radius: Float, mode: KMPBlurMode = KMPBlurMode.Normal)

enum class KMPBlurMode {
    Normal,
    Solid,
    Inner,
    Outer,
}
