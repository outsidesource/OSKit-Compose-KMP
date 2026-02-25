package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable

@Composable
expect fun KmpBackHandler(
    enabled: Boolean,
    onCancel: () -> Unit = {},
    onProgress: (KmpBackProgressEvent) -> Unit = { },
    onBackComplete: () -> Unit,
)

interface IKmpBackEvent {
    val progress: Float
    val touchX: Float
    val touchY: Float
    val swipeEdge: Int
}

data class KmpBackProgressEvent(
    override val progress: Float,
    override val touchX: Float,
    override val touchY: Float,
    override val swipeEdge: Int,
): IKmpBackEvent
