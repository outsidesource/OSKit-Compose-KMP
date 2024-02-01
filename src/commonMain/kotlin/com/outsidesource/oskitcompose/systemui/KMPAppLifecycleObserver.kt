package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.StateFlow

/**
 * Allows for retrieving/observing the current application state
 * This is primarily for determining if the application is in the foreground or the background
 */
interface IKMPAppLifecycleObserver {
    val lifecycle: StateFlow<KMPAppLifecycle>
    fun init(context: KMPAppLifecycleObserverContext)
}

enum class KMPAppLifecycle {
    // The application is in the background or minimized
    Background,

    // The application is unfocused or not ready to receive events
    Inactive,

    // The application is in the foreground
    Active,
}

expect object KMPAppLifecycleObserver : IKMPAppLifecycleObserver

expect class KMPAppLifecycleObserverContext