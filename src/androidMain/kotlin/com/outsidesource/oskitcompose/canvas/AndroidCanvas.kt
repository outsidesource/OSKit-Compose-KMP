package com.outsidesource.oskitcompose.canvas

import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.Paint

actual fun Paint.kmpBlur(radius: Float, mode: KmpBlurMode) {
    val frameworkPaint = asFrameworkPaint()
    frameworkPaint.maskFilter = BlurMaskFilter(
        radius,
        when (mode) {
            KmpBlurMode.Inner -> BlurMaskFilter.Blur.INNER
            KmpBlurMode.Outer -> BlurMaskFilter.Blur.OUTER
            KmpBlurMode.Normal -> BlurMaskFilter.Blur.NORMAL
            KmpBlurMode.Solid -> BlurMaskFilter.Blur.SOLID
        },
    )
}