package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {

    private val _state = MutableStateFlow(KMPAppLifecycle.Active)

    actual override val lifecycle: StateFlow<KMPAppLifecycle> = _state

    actual override fun init(context: KMPAppLifecycleObserverContext) {
        // TODO: WASM
    }
}

actual class KMPAppLifecycleObserverContext