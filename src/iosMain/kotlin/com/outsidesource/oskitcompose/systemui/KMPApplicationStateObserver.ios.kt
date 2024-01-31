package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState

actual object KMPApplicationStateObserver : IKMPApplicationStateObserver {

    private val _state = MutableStateFlow(UIApplication.sharedApplication.applicationState.toKMPApplicationState())

    override val state: StateFlow<KMPApplicationState> = _state

    override fun init(context: KMPApplicationStateObserverContext) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidBecomeActiveNotification,
            null,
            null
        ) {
            _state.tryEmit(KMPApplicationState.Active)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidEnterBackgroundNotification,
            null,
            null,
        ) {
            _state.tryEmit(KMPApplicationState.Background)
        }
    }
}

private fun UIApplicationState.toKMPApplicationState(): KMPApplicationState = when(this) {
    UIApplicationState.UIApplicationStateBackground -> KMPApplicationState.Background
    UIApplicationState.UIApplicationStateInactive -> KMPApplicationState.Inactive
    UIApplicationState.UIApplicationStateActive -> KMPApplicationState.Active
    else -> KMPApplicationState.Inactive
}

actual class KMPApplicationStateObserverContext