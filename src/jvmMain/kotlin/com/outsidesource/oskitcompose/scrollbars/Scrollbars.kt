package com.outsidesource.oskitcompose.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual typealias ScrollbarAdapter = androidx.compose.foundation.v2.ScrollbarAdapter

@Composable
actual fun rememberKmpScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter =
    androidx.compose.foundation.rememberScrollbarAdapter(
        scrollState = scrollState
    )

@Composable
actual fun rememberKmpScrollbarAdapter(
    scrollState: ScrollState,
): ScrollbarAdapter =
    androidx.compose.foundation.rememberScrollbarAdapter(
        scrollState = scrollState
    )

@Composable
actual fun KMPVerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
) {
    androidx.compose.foundation.VerticalScrollbar(
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
    androidx.compose.foundation.VerticalScrollbar(
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
    androidx.compose.foundation.HorizontalScrollbar(
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
    androidx.compose.foundation.HorizontalScrollbar(
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
