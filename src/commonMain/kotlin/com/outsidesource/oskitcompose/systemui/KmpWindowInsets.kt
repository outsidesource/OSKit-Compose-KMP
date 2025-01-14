package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt

data class KmpWindowInsets(
    private val top: Dp = 0.dp,
    private val right: Dp = 0.dp,
    private val bottom: Dp = 0.dp,
    private val left: Dp = 0.dp,
): WindowInsets {
    override fun getBottom(density: Density): Int = with(density) {
        bottom.toPx().roundToInt()
    }

    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = with(density) {
        left.toPx().roundToInt()
    }

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = with(density) {
        right.toPx().roundToInt()
    }

    override fun getTop(density: Density): Int = with(density) {
        top.toPx().roundToInt()
    }

    companion object
}

@get:Composable
expect val KmpWindowInsets.Companion.top: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.bottom: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.vertical: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.right: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.left: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.horizontal: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.all: WindowInsets
@get:Composable
expect val KmpWindowInsets.Companion.ime: WindowInsets

/**
 * [LocalKmpWindowInsets] Allows an application to override default WindowInsets.
 * This is particularly useful for Desktop as there are no predefined insets.
 */
val LocalKmpWindowInsets = staticCompositionLocalOf<KmpWindowInsetsHolder?> { null }

/**
 * Defines the total safe area bounds from which different window insets are derived
 */
data class KmpWindowInsetsHolder(
    private val top: Dp = 0.dp,
    private val right: Dp = 0.dp,
    private val bottom: Dp = 0.dp,
    private val left: Dp = 0.dp,
) {
    val topInsets = KmpWindowInsets(top = top)
    val bottomInsets = KmpWindowInsets(bottom = bottom)
    val rightInsets = KmpWindowInsets(right = right)
    val leftInsets = KmpWindowInsets(left = left)
    val verticalInsets = KmpWindowInsets(top = top, bottom = bottom)
    val horizontalInsets = KmpWindowInsets(left = left, right = right)
    val allInsets = KmpWindowInsets(top = top, right = right, bottom = bottom, left = left)
    val imeInsets = KmpWindowInsets(top = top, right = right, bottom = bottom, left = left)
}

internal val DefaultKmpWindowInsets = KmpWindowInsets()