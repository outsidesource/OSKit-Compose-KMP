package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.max

/**
 * WrappableRow allows for content to wrap to multiple lines with the specified [verticalSpacing]
 */
@Composable
fun WrappableRow(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalSpacing: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val hArrangement = remember<(Int, IntArray, LayoutDirection, Density, IntArray) -> Unit>(density, horizontalArrangement) {
        { totalSize, size, layoutDirection, density, outPosition ->
            with(horizontalArrangement) { density.arrange(totalSize, size, layoutDirection, outPosition) }
        }
    }

    Layout(
        modifier = modifier,
        content = content
    ) {measurables, constraints ->
        var ongoingWidth = 0
        var ongoingHeight = 0
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        val placeables: MutableList<MutableList<Placeable>> = mutableListOf(mutableListOf())

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val spacing =  horizontalArrangement.spacing.roundToPx()

            if (ongoingWidth + spacing + placeable.width > constraints.maxWidth) {
                rowHeights.add(ongoingHeight)
                rowWidths.add(ongoingWidth)
                placeables.add(mutableListOf())
                ongoingWidth = placeable.width
                ongoingHeight = placeable.height
            } else {
                ongoingWidth += spacing + placeable.width
                ongoingHeight = max(ongoingHeight, placeable.height)
            }

            placeables.last().add(placeable)
        }

        rowHeights.add(ongoingHeight)
        rowWidths.add(ongoingWidth)

        val totalWidth = rowWidths.maxOf { it }
        val totalHeight = rowHeights.sum() + ((rowHeights.size - 1) * verticalSpacing.roundToPx())

        layout(constraints.maxWidth, totalHeight) {
            var baseY = 0

            placeables.forEachIndexed { rowI, row ->
                val childrenWidths = IntArray(row.size) { i -> row[i].width }
                val mainAxisPositions = IntArray(row.size) { 0 }
                hArrangement(totalWidth, childrenWidths, LayoutDirection.Ltr, density, mainAxisPositions)

                row.forEachIndexed { i, placeable ->
                    placeable.placeRelative(IntOffset(
                        x = mainAxisPositions[i],
                        y = baseY,
                    ))
                }

                baseY += rowHeights[rowI] + verticalSpacing.roundToPx()
            }
        }
    }
}