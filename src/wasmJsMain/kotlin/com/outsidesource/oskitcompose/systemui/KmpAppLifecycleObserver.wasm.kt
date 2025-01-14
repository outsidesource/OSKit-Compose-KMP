package com.outsidesource.oskitcompose.systemui

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {

    private val _state = MutableStateFlow(KmpAppLifecycle.Active)

    actual override val lifecycle: StateFlow<KmpAppLifecycle> = _state

    actual override fun init(context: KmpAppLifecycleObserverContext) {
        window.addEventListener("visibilitychange") {
            if (document.visibilityState == "hidden") {
                _state.value = KmpAppLifecycle.Background
            } else if (document.visibilityState == "visible") {
                _state.value = KmpAppLifecycle.Active
            }
        }
    }
}

actual class KmpAppLifecycleObserverContext

private external object document {
    val visibilityState: String
}