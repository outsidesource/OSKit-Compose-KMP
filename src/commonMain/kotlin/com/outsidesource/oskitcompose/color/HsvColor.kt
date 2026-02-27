package com.outsidesource.oskitcompose.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt


/**
 * Represents HSV color (hue, saturation, brightness)
 *
 * @param hue 0-360
 * @param saturation 0-1
 * @param value 0-1
 * @param alpha 0-1
 */
@Immutable
data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float = 1f,
) {

    init {
        require(hue >= 0 && hue <= 360) { "Hue must be between 0 and 360" }
        require(saturation >= 0f && saturation <= 1f) { "Saturation must be between 0f and 1f" }
        require(value >= 0f && value <= 1f) { "Value must be between 0f and 1f" }
        require(alpha >= 0f && alpha <= 1f) { "Alpha must be between 0f and 1f" }
    }

    fun toColor(): Color {
        val aNorm = (alpha * 255).roundToInt().coerceIn(0, 255)
        val hNorm = ((hue % 360) + 360) % 360
        if (saturation < 1e-6f) {
            val gray = (value * 255).roundToInt().coerceIn(0, 255)
            return Color(gray, gray, gray, aNorm)
        }

        val hPrime = hNorm / 60f
        val sector = hPrime.toInt()
        val fraction = hPrime - sector

        val p = value * (1 - saturation)
        val q = value * (1 - saturation * fraction)
        val t = value * (1 - saturation * (1 - fraction))

        val (rf, gf, bf) = when (sector % 6) {
            0 -> Triple(value, t, p)
            1 -> Triple(q, value, p)
            2 -> Triple(p, value, t)
            3 -> Triple(p, q, value)
            4 -> Triple(t, p, value)
            5 -> Triple(value, p, q)
            else -> Triple(0f, 0f, 0f)
        }

        val r = (rf * 255).roundToInt().coerceIn(0, 255)
        val g = (gf * 255).roundToInt().coerceIn(0, 255)
        val b = (bf * 255).roundToInt().coerceIn(0, 255)

        return Color(r, g, b, aNorm)
    }

    companion object {
        val Black = HsvColor(0f, 0f, 0f)
        val White = HsvColor(0f, 0f, 1f)
        val Transparent = HsvColor(0f, 0f, 0f, 0f)
        val Red = HsvColor(0f, 1f, 1f)
        val Yellow = HsvColor(60f, 1f, 1f)
        val Green = HsvColor(120f, 1f, 1f)
        val Cyan = HsvColor(180f, 1f, 1f)
        val Blue = HsvColor(240f, 1f, 1f)
        val Magenta = HsvColor(300f, 1f, 1f)
        val Gray = HsvColor(0f, 0f, 0.53333336f)

        fun fromColor(color: Color) = color.toHsvColor()
    }
}

fun Color.toHsvColor(): HsvColor {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min

    var h = when {
        delta == 0f -> 0f
        max == red-> 60 * (((green - blue) / delta) % 6)
        max == green -> 60 * (((blue - red) / delta) + 2)
        else -> 60 * (((red - green) / delta) + 4)
    }
    if (h < 0) h += 360f

    val hNorm = (h + 360) % 360
    val s = if (max == 0f) 0f else delta / max
    val v = max

    return HsvColor(hNorm, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f), alpha)
}