package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState

actual object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {

    private val _state = MutableStateFlow(UIApplication.sharedApplication.applicationState.toKmpAppLifecycle())

    actual override val lifecycle: StateFlow<KmpAppLifecycle> = _state

    actual override fun init(context: KmpAppLifecycleObserverContext) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidBecomeActiveNotification,
            null,
            null
        ) {
            _state.tryEmit(KmpAppLifecycle.Active)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidEnterBackgroundNotification,
            null,
            null,
        ) {
            _state.tryEmit(KmpAppLifecycle.Background)
        }
    }
}

private fun UIApplicationState.toKmpAppLifecycle(): KmpAppLifecycle = when(this) {
    UIApplicationState.UIApplicationStateBackground -> KmpAppLifecycle.Background
    UIApplicationState.UIApplicationStateInactive -> KMPAppLifecycle.Inactive
    UIApplicationState.UIApplicationStateActive -> KMPAppLifecycle.Active
    else -> KMPAppLifecycle.Inactive
}

actual class KMPAppLifecycleObserverContext