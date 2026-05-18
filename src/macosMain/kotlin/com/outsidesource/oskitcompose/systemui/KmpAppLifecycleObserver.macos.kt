package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationDidBecomeActiveNotification
import platform.AppKit.NSApplicationDidHideNotification
import platform.AppKit.NSApplicationDidResignActiveNotification
import platform.Foundation.NSNotificationCenter

actual object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {

    private val _state = MutableStateFlow(getCurrentState())

    actual override val lifecycle: StateFlow<KmpAppLifecycle> = _state

    actual override fun init(context: KmpAppLifecycleObserverContext) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            NSApplicationDidBecomeActiveNotification,
            null,
            null
        ) {
            _state.tryEmit(KmpAppLifecycle.Active)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            NSApplicationDidResignActiveNotification,
            null,
            null,
        ) {
            _state.tryEmit(KmpAppLifecycle.Inactive)
        }

        NSNotificationCenter.defaultCenter.addObserverForName(
            NSApplicationDidHideNotification,
            null,
            null,
        ) {
            _state.tryEmit(KmpAppLifecycle.Background)
        }
    }
}

private fun getCurrentState(): KmpAppLifecycle {
    if (NSApplication.sharedApplication.active) return KmpAppLifecycle.Active
    if (NSApplication.sharedApplication.hidden) return KmpAppLifecycle.Background
    return KmpAppLifecycle.Inactive
}

actual class KmpAppLifecycleObserverContext