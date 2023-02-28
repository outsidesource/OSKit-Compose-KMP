package com.outsidesource.oskitcompose.color

import androidx.compose.ui.graphics.Color

fun Color.Companion.parseColor(string: String): Color {
    return string.replace("#", "").chunked(2).run {
        when (this.size) {
            4 -> Color(this[0].toInt(16), this[1].toInt(16), this[2].toInt(16), this[3].toInt(16))
            3 -> Color(this[0].toInt(16), this[1].toInt(16), this[2].toInt(16))
            else -> Black
        }
    }
}

fun Color.toLong(): Long = (value shr 32).toLong()