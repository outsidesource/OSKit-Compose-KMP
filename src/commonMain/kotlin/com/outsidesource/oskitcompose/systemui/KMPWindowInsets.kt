package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt

data class KMPWindowInsets(
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
expect val KMPWindowInsets.Companion.topInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.bottomInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.verticalInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.rightInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.leftInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.horizontalInsets: WindowInsets
@get:Composable
expect val KMPWindowInsets.Companion.allInsets: WindowInsets

/**
 * [LocalKMPWindowInsets] Allows an application to override default WindowInsets.
 * This is particularly useful for Desktop as there are no predefined insets.
 */
val LocalKMPWindowInsets = staticCompositionLocalOf<KMPWindowInsetsHolder?> { null }

/**
 * Defines the total safe area bounds from which different window insets are derived
 */
data class KMPWindowInsetsHolder(
    private val top: Dp = 0.dp,
    private val right: Dp = 0.dp,
    private val bottom: Dp = 0.dp,
    private val left: Dp = 0.dp,
) {
    val topInsets = KMPWindowInsets(top = top)
    val bottomInsets = KMPWindowInsets(bottom = bottom)
    val rightInsets = KMPWindowInsets(right = right)
    val leftInsets = KMPWindowInsets(left = left)
    val verticalInsets = KMPWindowInsets(top = top, bottom = bottom)
    val horizontalInsets = KMPWindowInsets(left = left, right = right)
    val allInsets = KMPWindowInsets(top = top, right = right, bottom = bottom, left = left)
}

internal val DefaultKMPWindowInsets = KMPWindowInsets()