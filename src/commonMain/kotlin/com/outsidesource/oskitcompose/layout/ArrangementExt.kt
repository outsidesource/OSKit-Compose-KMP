package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

/**
 * Similar to spacedBetween but with guaranteed padding in between composables.
 */
@Stable
fun Arrangement.spaceBetweenPadded(padding: Dp) = object : Arrangement.HorizontalOrVertical {
    override val spacing = padding

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) = placeSpaceBetween(totalSize, sizes, outPositions)

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        outPositions: IntArray
    ) = placeSpaceBetween(totalSize, sizes, outPositions)

    override fun toString() = "Arrangement#SpaceBetweenPadded"

    fun placeSpaceBetween(
        totalSize: Int,
        size: IntArray,
        outPosition: IntArray,
    ) {
        val consumedSize = size.fold(0) { a, b -> a + b }
        val gapSize = if (size.size > 1) (totalSize - consumedSize).toFloat() / (size.size - 1) else 0f
        var current = 0f

        size.forEachIndexed { index, it ->
            outPosition[index] = current.roundToInt()
            current += it.toFloat() + gapSize
        }
    }
}