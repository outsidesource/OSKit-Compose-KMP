package com.outsidesource.oskitcompose.systemui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KMPApplicationStateObserver : IKMPApplicationStateObserver {

    private val _state = MutableStateFlow(ProcessLifecycleOwner.get().lifecycle.currentState.toKMPApplicationState())

    override val state: StateFlow<KMPApplicationState> = _state

    override fun init(context: KMPApplicationStateObserverContext) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> _state.tryEmit(KMPApplicationState.Active)
                        Lifecycle.Event.ON_STOP -> _state.tryEmit(KMPApplicationState.Background)
                        else -> {}
                    }
                }
            },
        )
    }
}

private fun Lifecycle.State.toKMPApplicationState() = when(this) {
    Lifecycle.State.CREATED,
    Lifecycle.State.INITIALIZED,
    Lifecycle.State.RESUMED,
    Lifecycle.State.STARTED -> KMPApplicationState.Active
    Lifecycle.State.DESTROYED -> KMPApplicationState.Background
}

actual class KMPApplicationStateObserverContext