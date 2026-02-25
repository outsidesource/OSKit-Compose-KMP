package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
actual fun KmpBackHandler(
    enabled: Boolean,
    onCancel: () -> Unit,
    onProgress: (KmpBackProgressEvent) -> Unit,
    onBackComplete: () -> Unit,
) {
    val navState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navState,
        isBackEnabled = enabled,
        onBackCancelled = onCancel,
        onBackCompleted = onBackComplete,
    )

    LaunchedEffect(navState.transitionState) {
        val transitionState = navState.transitionState
        if (transitionState !is NavigationEventTransitionState.InProgress) return@LaunchedEffect

        val event = KmpBackProgressEvent(
            progress = transitionState.latestEvent.progress,
            touchX = transitionState.latestEvent.touchX,
            touchY = transitionState.latestEvent.touchY,
            swipeEdge = transitionState.latestEvent.swipeEdge,
        )
        onProgress(event)
    }
}