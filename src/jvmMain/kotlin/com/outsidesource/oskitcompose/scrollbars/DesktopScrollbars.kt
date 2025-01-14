package com.outsidesource.oskitcompose.scrollbars

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual typealias ScrollbarAdapter = androidx.compose.foundation.v2.ScrollbarAdapter

@Composable
actual fun rememberKmpScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter =
    rememberScrollbarAdapter(
        scrollState = scrollState
    )

@Composable
actual fun rememberKmpScrollbarAdapter(
    scrollState: ScrollState,
): ScrollbarAdapter =
    rememberScrollbarAdapter(
        scrollState = scrollState
    )

@Composable
actual fun KmpVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = adapter
    )
}

@Composable
actual fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = adapter,
        style = androidx.compose.foundation.ScrollbarStyle(
            shape = style.shape,
            thickness = style.thickness,
            hoverDurationMillis = style.hoverDurationMillis,
            minimalHeight = style.minimalHeight,
            unhoverColor = style.unhoverColor,
            hoverColor = style.hoverColor,
        ),
    )
}

@Composable
actual fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
) {
    HorizontalScrollbar(
        modifier = modifier,
        adapter = adapter
    )
}

@Composable
actual fun KMPHorizontalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter,
    style: KMPScrollbarStyle,
) {
    HorizontalScrollbar(
        modifier = modifier,
        adapter = adapter,
        style = androidx.compose.foundation.ScrollbarStyle(
            shape = style.shape,
            thickness = style.thickness,
            hoverDurationMillis = style.hoverDurationMillis,
            minimalHeight = style.minimalHeight,
            unhoverColor = style.unhoverColor,
            hoverColor = style.hoverColor,
        ),
    )
}
