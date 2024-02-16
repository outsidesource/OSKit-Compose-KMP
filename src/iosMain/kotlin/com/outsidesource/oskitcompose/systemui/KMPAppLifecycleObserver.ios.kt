package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState

actual object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {

    private val _state = MutableStateFlow(UIApplication.sharedApplication.applicationState.toKMPAppLifecycle())

    override val lifecycle: StateFlow<KMPAppLifecycle> = _state

    override fun init(context: KMPAppLifecycleObserverContext) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidBecomeActiveNotification,
            null,
            null
        ) {
            _state.tryEmit(KMPAppLifecycle.Active)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidEnterBackgroundNotification,
            null,
            null,
        ) {
            _state.tryEmit(KMPAppLifecycle.Background)
        }
    }
}

private fun UIApplicationState.toKMPAppLifecycle(): KMPAppLifecycle = when(this) {
    UIApplicationState.UIApplicationStateBackground -> KMPAppLifecycle.Background
    UIApplicationState.UIApplicationStateInactive -> KMPAppLifecycle.Inactive
    UIApplicationState.UIApplicationStateActive -> KMPAppLifecycle.Active
    else -> KMPAppLifecycle.Inactive
}

actual class KMPAppLifecycleObserverContext