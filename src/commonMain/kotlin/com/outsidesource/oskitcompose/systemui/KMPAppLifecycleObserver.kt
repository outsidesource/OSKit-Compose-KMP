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
    /**
     * Android: App is in the background
     * Desktop: App is minimized
     * iOS: App is in background
     * WASM: App is in background
     */
    Background,

    /**
     * Android: Unused
     * Desktop: Application is unfocused
     * iOS: App is not ready to receive events (iOS)
     * WASM: Unused
     */
    Inactive,

    /**
     * All targets: App is in the foreground
     */
    Active,
}

expect object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {
    override val lifecycle: StateFlow<KMPAppLifecycle>
    override fun init(context: KMPAppLifecycleObserverContext)
}

expect class KMPAppLifecycleObserverContext