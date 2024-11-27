package com.outsidesource.oskitcompose.systemui

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {

    private val _state = MutableStateFlow(KMPAppLifecycle.Active)

    actual override val lifecycle: StateFlow<KMPAppLifecycle> = _state

    actual override fun init(context: KMPAppLifecycleObserverContext) {
        window.addEventListener("visibilitychange") {
            if (document.visibilityState == "hidden") {
                _state.value = KMPAppLifecycle.Background
            } else if (document.visibilityState == "visible") {
                _state.value = KMPAppLifecycle.Active
            }
        }
    }
}

actual class KMPAppLifecycleObserverContext

private external object document {
    val visibilityState: String
}