package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.min

/**
 * FixedTableRow specifies a row with columns of fixed width based on the passed in percentage
 */
@Composable
fun FixedTableRow(
    columnSizes: List<Float>,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val arrangement = remember<(Int, IntArray, LayoutDirection, Density, IntArray) -> Unit>(density, horizontalArrangement) {
        { totalSize, size, layoutDirection, density, outPosition ->
            with(horizontalArrangement) { density.arrange(totalSize, size, layoutDirection, outPosition) }
        }
    }

    Layout(
        modifier = modifier,
        content = content
    ) {measurables, constraints ->
        val placeables = measurables.mapIndexed { i, measurable ->
            val percentage = columnSizes.getOrNull(i) ?: 0f
            val spacing = if (i == 0 || i == measurables.size - 1) horizontalArrangement.spacing / 2 else horizontalArrangement.spacing
            val width = (percentage * (constraints.maxWidth)).toInt() - spacing.roundToPx()

            measurable.measure(constraints.copy(
                minWidth = width,
                maxWidth = width,
                minHeight = min(constraints.minHeight, measurable.minIntrinsicHeight(width))
            ))
        }

        val maxHeight = if (constraints.maxHeight == Constraints.Infinity) {
            placeables.maxOf { it.height  }
        } else {
            constraints.maxHeight
        }

        layout(constraints.maxWidth, maxHeight) {
            val childrenSizes = IntArray(measurables.size) { i -> placeables[i].width }
            val mainAxisPositions = IntArray(measurables.size) { 0 }
            arrangement(constraints.maxWidth, childrenSizes, LayoutDirection.Ltr, density, mainAxisPositions)

            placeables.forEachIndexed { i, placeable ->
                placeable.placeRelative(IntOffset(x = mainAxisPositions[i], y = verticalAlignment.align(placeable.height, maxHeight)))
            }
        }
    }
}