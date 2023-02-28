package com.outsidesource.oskitcompose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * FlexRow allows items in a row to expand to fill the available height.
 *
 * Any child with the crossAxisFlex() modifier will be constrained to the largest height of any child without
 * crossAxisFlex() set. Children with crossAxisFlex() set are measured last.
 *
 * Any child with the weight() modifier will be weighted similarly to the Row component
 *
 * Any child with the mainAxisWidth modifier will be treated as a fixed width
 */
@Composable
fun FlexRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable FlexRowLayoutScope.() -> Unit
) {
    val density = LocalDensity.current
    val arrangement = remember<(Int, IntArray, LayoutDirection, Density, IntArray) -> Unit>(density, horizontalArrangement) {
        { totalSize, size, layoutDirection, density, outPosition ->
            with(horizontalArrangement) { density.arrange(totalSize, size, layoutDirection, outPosition) }
        }
    }

    Layout(
        modifier = modifier,
        content = { FlexRowLayoutScope.content() },
    ) { measurables, constraints ->
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        var totalWeight = 0f
        var fixedSize = horizontalArrangement.spacing * (measurables.size - 1)
        var largestHeight = 0

        // Gather data
        measurables.forEachIndexed { i, measurable ->
            val parentData = measurable.parentData
            if (parentData !is FlexLayoutParentData) {
                placeables[i] = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth - fixedSize.roundToPx()))
                fixedSize += placeables[i]?.width?.toDp() ?: 0.dp
                largestHeight = max(placeables[i]?.height ?: 0, largestHeight)
            } else if (parentData.weight > 0) {
                totalWeight += parentData.weight
            } else if (parentData.mainAxisSize > 0.dp) {
                fixedSize += parentData.mainAxisSize
            }
        }

        val availableWidth = constraints.maxWidth - fixedSize.roundToPx()

        // Measure non-crossAxisFlex children
        measurables.forEachIndexed { i, measurable ->
            val parentData = measurable.parentData as? FlexLayoutParentData ?: return@forEachIndexed
            if (parentData.crossAxisFlex > 0) return@forEachIndexed

            if (parentData.weight > 0) {
                placeables[i] = measurable.measure(constraints.copy(maxWidth = (availableWidth * (parentData.weight / totalWeight)).roundToInt()))
            } else if (parentData.mainAxisSize > 0.dp) {
                placeables[i] = measurable.measure(constraints.copy(maxWidth = min(availableWidth, parentData.mainAxisSize.roundToPx())))
            } else {
                placeables[i] = measurable.measure(constraints.copy(maxWidth = availableWidth))
            }

            largestHeight = max(placeables[i]?.height ?: 0, largestHeight)
        }

        // Measure crossAxisFlex children
        measurables.forEachIndexed { i, measurable ->
            val parentData = measurable.parentData as? FlexLayoutParentData ?: return@forEachIndexed
            if (parentData.crossAxisFlex == 0f) return@forEachIndexed

            if (parentData.weight > 0) {
                placeables[i] = measurable.measure(constraints.copy(
                    maxWidth = (availableWidth * (parentData.weight / totalWeight)).roundToInt(),
                    maxHeight = (largestHeight * parentData.crossAxisFlex).roundToInt()
                ))
            } else if (parentData.mainAxisSize > 0.dp) {
                placeables[i] = measurable.measure(constraints.copy(
                    maxWidth = min(availableWidth, parentData.mainAxisSize.roundToPx()),
                    maxHeight = (largestHeight * parentData.crossAxisFlex).roundToInt())
                )
            } else {
                placeables[i] = measurable.measure(constraints.copy(
                    maxWidth = availableWidth,
                    maxHeight = (largestHeight * parentData.crossAxisFlex).roundToInt())
                )
            }
        }

        layout(constraints.maxWidth, largestHeight) {
            val mainAxisPositions = IntArray(measurables.size) { 0 }
            val childrenSizes = IntArray(measurables.size) { i -> placeables[i]?.width ?: 0 }
            arrangement(constraints.maxWidth, childrenSizes, LayoutDirection.Ltr, density, mainAxisPositions)

            placeables.forEachIndexed { i, placeable ->
                if (placeable == null) return@forEachIndexed
                placeable.place(mainAxisPositions[i], 0)
            }
        }
    }
}

data class FlexLayoutParentData(
    var weight: Float = 0f,
    var crossAxisFlex: Float = 0f,
    var mainAxisSize: Dp = 0.dp,
)

@Immutable
object FlexRowLayoutScope {
    @Stable
    fun Modifier.weight(weight: Float): Modifier = then(
        object : ParentDataModifier {
            override fun Density.modifyParentData(parentData: Any?): Any =
                (parentData as? FlexLayoutParentData ?: FlexLayoutParentData()).also { it.weight = weight }
        }
    ).fillMaxWidth()

    @Stable
    fun Modifier.mainAxisWidth(width: Dp): Modifier = then(
        object : ParentDataModifier {
            override fun Density.modifyParentData(parentData: Any?): Any =
                (parentData as? FlexLayoutParentData ?: FlexLayoutParentData()).also { it.mainAxisSize = width }
        }
    ).width(width)

    @Stable
    fun Modifier.crossAxisFlex(fraction: Float = 1f): Modifier = then(
        object : ParentDataModifier {
            override fun Density.modifyParentData(parentData: Any?): Any =
                (parentData as? FlexLayoutParentData ?: FlexLayoutParentData()).also { it.crossAxisFlex = fraction }
        }
    ).fillMaxHeight()
}