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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun VerticalGrid(
    modifier: Modifier = Modifier,
    columns: (Constraints) -> Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        val realizedColumns = maxOf(1, columns(constraints))
        val rowCount = (measurables.size + realizedColumns - 1) / realizedColumns
        val rowHeights = IntArray(rowCount)
        val hSpacing = (horizontalArrangement.spacing * maxOf(0, realizedColumns - 1)).roundToPx()
        val vSpacing = (verticalArrangement.spacing * maxOf(0, rowCount - 1)).roundToPx()

        val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val hPadding = startPadding + contentPadding.calculateEndPadding(layoutDirection).roundToPx()
        val vPadding = topPadding + contentPadding.calculateBottomPadding().roundToPx()

        val cellWidth = maxOf((constraints.maxWidth - hSpacing - hPadding) / realizedColumns, 0)
        val childConstraints = constraints.copy(maxWidth = cellWidth, minWidth = cellWidth, minHeight = 0)

        for (i in 0..<rowCount) {
            var maxItemHeight = 0
            for (j in 0..<realizedColumns) {
                val itemIndex = (realizedColumns * i) + j
                if (itemIndex >= measurables.size) break

                val placeable = measurables[itemIndex].measure(childConstraints)
                placeables[itemIndex] = placeable
                maxItemHeight = maxOf(placeable.height, maxItemHeight)
            }
            rowHeights[i] = maxItemHeight
        }

        val calculatedHeight = rowHeights.sum() + vSpacing + vPadding
        val layoutWidth = constraints.maxWidth
        val layoutHeight =
            when {
                constraints.hasFixedHeight || constraints.hasBoundedHeight ->
                    calculatedHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
                else -> calculatedHeight.coerceAtLeast(constraints.minHeight)
            }

        layout(layoutWidth, layoutHeight) {
            val yPositions = IntArray(rowCount)
            with(verticalArrangement) { density.arrange(layoutHeight - vPadding, rowHeights, yPositions) }

            val xPositions = IntArray(realizedColumns)
            val childrenWidths = IntArray(realizedColumns)

            for (i in 0..<rowCount) {
                for (j in 0..<realizedColumns) {
                    val itemIndex = (i * realizedColumns) + j
                    childrenWidths[j] = if (itemIndex < measurables.size) cellWidth else 0
                }

                with(horizontalArrangement) {
                    density.arrange(layoutWidth - hPadding, childrenWidths, layoutDirection, xPositions)
                }

                for (j in 0..<realizedColumns) {
                    val itemIndex = (i * realizedColumns) + j
                    if (itemIndex >= measurables.size) break
                    val placeable = placeables[itemIndex]!!
                    placeable.place(startPadding + xPositions[j], topPadding + yPositions[i])
                }
            }
        }
    }
}