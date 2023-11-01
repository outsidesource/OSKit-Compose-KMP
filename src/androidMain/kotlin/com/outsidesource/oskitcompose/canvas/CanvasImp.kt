package com.outsidesource.oskitcompose.canvas

import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.Paint

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