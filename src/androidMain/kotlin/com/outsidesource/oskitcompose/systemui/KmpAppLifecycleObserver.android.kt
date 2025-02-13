package com.outsidesource.oskitcompose.systemui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {

    private val _state = MutableStateFlow(ProcessLifecycleOwner.get().lifecycle.currentState.toKmpAppLifecycle())

    actual override val lifecycle: StateFlow<KmpAppLifecycle> = _state

    actual override fun init(context: KmpAppLifecycleObserverContext) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> _state.tryEmit(KmpAppLifecycle.Active)
                        Lifecycle.Event.ON_STOP -> _state.tryEmit(KmpAppLifecycle.Background)
                        else -> {}
                    }
                }
            },
        )
    }
}

private fun Lifecycle.State.toKmpAppLifecycle() = when(this) {
    Lifecycle.State.CREATED,
    Lifecycle.State.INITIALIZED,
    Lifecycle.State.RESUMED,
    Lifecycle.State.STARTED -> KmpAppLifecycle.Active
    Lifecycle.State.DESTROYED -> KmpAppLifecycle.Background
}

actual class KmpAppLifecycleObserverContext