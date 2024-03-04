package com.outsidesource.oskitcompose.modifier

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

@Stable
fun Modifier.defaultMaxSize(
    maxWidth: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified
) = this.then(
    UnspecifiedConstraintsElement(
        maxWidth = maxWidth,
        maxHeight = maxHeight
    )
)

private class UnspecifiedConstraintsElement(
    val maxWidth: Dp = Dp.Unspecified,
    val maxHeight: Dp = Dp.Unspecified,
) : ModifierNodeElement<UnspecifiedConstraintsNode>() {
    override fun create(): UnspecifiedConstraintsNode = UnspecifiedConstraintsNode(
        maxWidth = maxWidth,
        maxHeight = maxHeight
    )

    override fun update(node: UnspecifiedConstraintsNode) {
        node.maxWidth = maxWidth
        node.maxHeight = maxHeight
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UnspecifiedConstraintsElement) return false
        return maxWidth == other.maxWidth && maxHeight == other.maxHeight
    }

    override fun hashCode() = maxWidth.hashCode() * 31 + maxHeight.hashCode()
}

private class UnspecifiedConstraintsNode(
    var maxWidth: Dp = Dp.Unspecified,
    var maxHeight: Dp = Dp.Unspecified
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val wrappedConstraints = Constraints(
            minWidth = constraints.minWidth,
            maxWidth = if (maxWidth != Dp.Unspecified) {
                maxWidth.roundToPx().coerceIn(constraints.minWidth, constraints.maxWidth)
            } else {
                constraints.maxWidth
            },
            minHeight = constraints.minHeight,
            maxHeight = if (maxHeight != Dp.Unspecified) {
                maxHeight.roundToPx().coerceIn(constraints.minHeight, constraints.maxHeight)
            } else {
                constraints.maxHeight
            },
        )

        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        val intrinsicValue = measurable.minIntrinsicWidth(height)
        return if (maxWidth != Dp.Unspecified) intrinsicValue.coerceAtMost(maxWidth.roundToPx()) else intrinsicValue
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        val intrinsicValue = measurable.maxIntrinsicWidth(height)
        return if (maxWidth != Dp.Unspecified) intrinsicValue.coerceAtMost(maxWidth.roundToPx()) else intrinsicValue
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        val intrinsicValue = measurable.minIntrinsicHeight(width)
        return if (maxHeight != Dp.Unspecified) intrinsicValue.coerceAtMost(maxHeight.roundToPx()) else intrinsicValue
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        val intrinsicValue = measurable.maxIntrinsicHeight(width)
        return if (maxHeight != Dp.Unspecified) intrinsicValue.coerceAtMost(maxHeight.roundToPx()) else intrinsicValue
    }
}