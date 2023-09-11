package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.Paint

expect fun Paint.kmpBlur(radius: Float, mode: KMPBlurMode = KMPBlurMode.Normal)

enum class KMPBlurMode {
    Normal,
    Solid,
    Inner,
    Outer,
}
