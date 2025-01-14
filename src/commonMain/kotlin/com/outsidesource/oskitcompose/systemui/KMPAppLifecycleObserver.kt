package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.StateFlow

/**
 * Allows for retrieving/observing the current application state
 * This is primarily for determining if the application is in the foreground or the background
 */
interface IKmpAppLifecycleObserver {
    val lifecycle: StateFlow<KmpAppLifecycle>
    fun init(context: KmpAppLifecycleObserverContext)
}

enum class KmpAppLifecycle {
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

expect object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {
    override val lifecycle: StateFlow<KmpAppLifecycle>
    override fun init(context: KmpAppLifecycleObserverContext)
}

expect class KmpAppLifecycleObserverContext