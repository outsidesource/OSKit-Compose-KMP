package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun VerticalGrid(
    modifier: Modifier,
    columns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val localHArrangement = remember<(Int, IntArray, LayoutDirection, Density, IntArray) -> Unit>(density, horizontalArrangement) {
        { totalSize, sizes, layoutDirection, density, outPosition ->
            with(horizontalArrangement) { density.arrange(totalSize, sizes, layoutDirection, outPosition) }
        }
    }

    val localVArrangement = remember<(Int, IntArray, Density, IntArray) -> Unit>(density, verticalArrangement) {
        { totalSize, sizes, density, outPosition ->
            with(verticalArrangement) { density.arrange(totalSize, sizes, outPosition) }
        }
    }

    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        val rowHeights = IntArray(ceil(measurables.size / columns.toFloat()).toInt())
        val hSpacing = (horizontalArrangement.spacing * (columns - 1)).roundToPx()
        val vSpacing = (((measurables.size / columns) - 1) * verticalArrangement.spacing).roundToPx()
        val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val hPadding = startPadding + contentPadding.calculateEndPadding(layoutDirection).roundToPx()
        val vPadding = topPadding + contentPadding.calculateBottomPadding().roundToPx()

        for (i in 0..< ceil(measurables.size / columns.toFloat()).toInt()) {
            var maxItemHeight = 0

            for (j in 0..< columns) {
                val itemIndex = (columns * i) + j
                if (itemIndex >= measurables.size) break
                val measurable = measurables[itemIndex]
                val placeable = measurable.measure(
                    constraints.copy(
                        maxWidth = (constraints.maxWidth - hSpacing - hPadding) / columns,
                        minWidth = (constraints.maxWidth - hSpacing - hPadding) / columns,
                        minHeight = 0
                    )
                )
                placeables[itemIndex] = placeable
                maxItemHeight = max(placeable.height, maxItemHeight)
            }
            rowHeights[i] = maxItemHeight
        }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = max(constraints.minHeight, rowHeights.sumOf { it } + vSpacing + vPadding)

        layout(layoutWidth, layoutHeight) {
            val yPositions = IntArray(ceil(placeables.size / columns.toFloat()).toInt())

            for (i in 0..< ceil(placeables.size / columns.toFloat()).toInt()) {
                val xPositions = IntArray(columns)
                val childrenWidths = IntArray(columns) { j -> placeables.getOrNull((i * columns) + j)?.width ?: 0 }

                localHArrangement(layoutWidth, childrenWidths, layoutDirection, density, xPositions)
                localVArrangement(layoutHeight - vPadding, rowHeights, density, yPositions)

                for (j in 0..< columns) {
                    val itemIndex = (i * columns) + j
                    val placeable = placeables.getOrNull(itemIndex) ?: break
                    placeable.place(startPadding + xPositions[j], topPadding + yPositions[i])
                }
            }
        }
    }
}