package com.outsidesource.oskitcompose.animation

import androidx.compose.animation.core.CubicBezierEasing

val CubicBezierEase = CubicBezierEasing(.25f, .1f, .25f, 1f)
val CubicBezierEaseIn = CubicBezierEasing(.42f, .0f, 1f, 1f)
val CubicBezierEaseOut = CubicBezierEasing(0f, 0f, .58f, 1f)
val CubicBezierEaseOutCirc = CubicBezierEasing(.08f, 0.82f, .17f, 1f)
val CubicBezierEaseInEaseOut = CubicBezierEasing(.42f, 0f, .58f, 1f)