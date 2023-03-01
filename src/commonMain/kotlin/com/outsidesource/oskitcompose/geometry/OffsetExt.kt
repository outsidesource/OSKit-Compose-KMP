package com.outsidesource.oskitcompose.geometry

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitkmp.lib.snapTo
import kotlin.math.roundToInt

fun Offset.toDpOffset(density: Density): DpOffset {
    val adjustedOffset = this / density.density
    return DpOffset(adjustedOffset.x.dp, adjustedOffset.y.dp)
}

fun Offset.toDpOffset(density: Float): DpOffset {
    val adjustedOffset = this / density
    return DpOffset(adjustedOffset.x.dp, adjustedOffset.y.dp)
}

fun Offset.snapTo(value: Float): Offset {
    return this.copy(x = x.snapTo(value), y = y.snapTo(value))
}

fun DpOffset.snapTo(value: Float): DpOffset {
    return this.copy(x = x.value.snapTo(value).dp, y = y.value.snapTo(value).dp)
}

operator fun DpOffset.times(value: Float): DpOffset = DpOffset(x * value, y * value)
operator fun DpOffset.div(value: Float): DpOffset = DpOffset(x / value, y / value)
operator fun DpOffset.rem(value: Float): DpOffset = DpOffset(x % value, y % value)
fun DpOffset.toIntOffset(density: Density): IntOffset = with(density) { IntOffset(x.roundToPx(), y.roundToPx()) }
fun DpOffset.toIntOffset(density: Float): IntOffset = IntOffset((x.value * density).roundToInt(), (y.value * density).roundToInt())
fun DpOffset.toOffset(density: Density): Offset = with(density) { Offset(x.toPx(), y.toPx()) }
fun DpOffset.toOffset(density: Float): Offset = Offset(x.value * density, y.value * density)

val DpOffset.Companion.VectorConverter: TwoWayConverter<DpOffset, AnimationVector2D>
    get() = DpOffsetVectorConverter

private val DpOffsetVectorConverter: TwoWayConverter<DpOffset, AnimationVector2D> = TwoWayConverter(
    convertToVector = { AnimationVector2D(it.x.value, it.y.value) },
    convertFromVector = { DpOffset(it.v1.dp, it.v2.dp) }
)

fun Offset.toIntOffset(): IntOffset = IntOffset(x.toInt(), y.toInt())