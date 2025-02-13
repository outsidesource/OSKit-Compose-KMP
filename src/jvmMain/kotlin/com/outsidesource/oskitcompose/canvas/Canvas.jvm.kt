package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.Paint
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

actual fun Paint.kmpBlur(radius: Float, mode: KmpBlurMode) {
    val frameworkPaint = asFrameworkPaint()
    frameworkPaint.maskFilter = MaskFilter.makeBlur(
        sigma = radius,
        mode = when (mode) {
            KmpBlurMode.Inner -> FilterBlurMode.INNER
            KmpBlurMode.Outer -> FilterBlurMode.OUTER
            KmpBlurMode.Normal -> FilterBlurMode.NORMAL
            KmpBlurMode.Solid -> FilterBlurMode.SOLID
        },
    )
}
