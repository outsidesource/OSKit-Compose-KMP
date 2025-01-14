package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.Paint

expect fun Paint.kmpBlur(radius: Float, mode: KmpBlurMode = KmpBlurMode.Normal)

enum class KmpBlurMode {
    Normal,
    Solid,
    Inner,
    Outer,
}
