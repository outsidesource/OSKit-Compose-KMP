package com.outsidesource.oskitcompose.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual interface ScrollbarAdapter

@Composable
actual fun rememberKmpScrollbarAdapter(scrollState: LazyListState): ScrollbarAdapter = object : ScrollbarAdapter {}

@Composable
actual fun rememberKmpScrollbarAdapter(
    scrollState: ScrollState,
): ScrollbarAdapter =
    object : ScrollbarAdapter {}

@Composable
actual fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
) { }

@Composable
actual fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
) { }

@Composable
actual fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
) { }

@Composable
actual fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
) { }
