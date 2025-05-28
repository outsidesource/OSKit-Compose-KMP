package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
expect fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
expect fun KmpPredictiveBackHandler(enabled: Boolean = true, onBack: suspend (Flow<IKmpBackEvent>) -> Unit)

interface IKmpBackEvent {
    val progress: Float
    val touchX: Float
    val touchY: Float
    val swipeEdge: Int
}

data class KmpBackEvent(
    override val progress: Float,
    override val touchX: Float,
    override val touchY: Float,
    override val swipeEdge: Int,
): IKmpBackEvent
