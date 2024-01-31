package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.StateFlow

/**
 * Allows for retrieving/observing the current application state
 * This is primarily for determining if the application is in the foreground or the background
 */
interface IKMPApplicationStateObserver {
    val state: StateFlow<KMPApplicationState>
    fun init(context: KMPApplicationStateObserverContext)
}

enum class KMPApplicationState {
    // The application is in the background or minimized
    Background,

    // The application is unfocused or not ready to receive events
    Inactive,

    // The application is in the foreground
    Active,
}

expect object KMPApplicationStateObserver : IKMPApplicationStateObserver

expect class KMPApplicationStateObserverContext