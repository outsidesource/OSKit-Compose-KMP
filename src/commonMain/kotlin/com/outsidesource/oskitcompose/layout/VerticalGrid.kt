package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun VerticalGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    content: @Composable () -> Unit,
) {
    require(columns > 0) { "columns must be > 0, was $columns" }
    val density = LocalDensity.current

    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        val rowHeights = IntArray(ceil(measurables.size / columns.toFloat()).toInt())
        val rowCount = ceil(measurables.size / columns.toFloat()).toInt()
        val hSpacing = (horizontalArrangement.spacing * (columns - 1)).roundToPx()
        val vSpacing = ((rowCount - 1) * verticalArrangement.spacing).roundToPx()
        val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val hPadding = startPadding + contentPadding.calculateEndPadding(layoutDirection).roundToPx()
        val vPadding = topPadding + contentPadding.calculateBottomPadding().roundToPx()
        val cellWidth = (constraints.maxWidth - hSpacing - hPadding) / columns

        for (i in 0..< rowCount) {
            var maxItemHeight = 0

            for (j in 0..< columns) {
                val itemIndex = (columns * i) + j
                if (itemIndex >= measurables.size) break
                val measurable = measurables[itemIndex]
                val placeable = measurable.measure(
                    constraints.copy(maxWidth = cellWidth, minWidth = cellWidth, minHeight = 0)
                )
                placeables[itemIndex] = placeable
                maxItemHeight = max(placeable.height, maxItemHeight)
            }
            rowHeights[i] = maxItemHeight
        }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = max(constraints.minHeight, rowHeights.sumOf { it } + vSpacing + vPadding)

        layout(layoutWidth, layoutHeight) {
            val yPositions = IntArray(rowCount)
            with(verticalArrangement) {
                density.arrange(layoutHeight - vPadding, rowHeights, yPositions)
            }

            for (i in 0..< rowCount) {
                val xPositions = IntArray(columns)
                val childrenWidths = IntArray(columns) { j -> placeables.getOrNull((i * columns) + j)?.width ?: 0 }

                with(horizontalArrangement) {
                    density.arrange(layoutWidth, childrenWidths, layoutDirection, xPositions)
                }

                for (j in 0..< columns) {
                    val itemIndex = (i * columns) + j
                    val placeable = placeables.getOrNull(itemIndex) ?: break
                    placeable.place(startPadding + xPositions[j], topPadding + yPositions[i])
                }
            }
        }
    }
}