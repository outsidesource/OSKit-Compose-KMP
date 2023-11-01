package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.Paint
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

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
