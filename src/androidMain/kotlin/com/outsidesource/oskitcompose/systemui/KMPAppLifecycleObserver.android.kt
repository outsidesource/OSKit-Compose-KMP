package com.outsidesource.oskitcompose.systemui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {

    private val _state = MutableStateFlow(ProcessLifecycleOwner.get().lifecycle.currentState.toKMPAppLifecycle())

    override val lifecycle: StateFlow<KMPAppLifecycle> = _state

    override fun init(context: KMPAppLifecycleObserverContext) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> _state.tryEmit(KMPAppLifecycle.Active)
                        Lifecycle.Event.ON_STOP -> _state.tryEmit(KMPAppLifecycle.Background)
                        else -> {}
                    }
                }
            },
        )
    }
}

private fun Lifecycle.State.toKMPAppLifecycle() = when(this) {
    Lifecycle.State.CREATED,
    Lifecycle.State.INITIALIZED,
    Lifecycle.State.RESUMED,
    Lifecycle.State.STARTED -> KMPAppLifecycle.Active
    Lifecycle.State.DESTROYED -> KMPAppLifecycle.Background
}

actual class KMPAppLifecycleObserverContext