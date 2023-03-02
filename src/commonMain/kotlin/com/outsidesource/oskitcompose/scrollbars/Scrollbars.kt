package com.outsidesource.oskitcompose.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect interface ScrollbarAdapter

@Composable
expect fun rememberKmpScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter

@Composable
expect fun rememberKmpScrollbarAdapter(
    scrollState: ScrollState,
): ScrollbarAdapter

@Composable
expect fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
)

@Composable
expect fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
)

@Composable
expect fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
)

@Composable
expect fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
)

data class KMPScrollbarStyle(
    val minimalHeight: Dp = 16.dp,
    val thickness: Dp = 8.dp,
    val shape: Shape = RoundedCornerShape(4.dp),
    val hoverDurationMillis: Int = 300,
    val unhoverColor: Color = Color.Black.copy(alpha = 0.12f),
    val hoverColor: Color = Color.Black.copy(alpha = 0.50f),
)