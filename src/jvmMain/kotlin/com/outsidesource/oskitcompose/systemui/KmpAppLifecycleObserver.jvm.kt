package com.outsidesource.oskitcompose.systemui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.awt.Frame
import java.awt.Window
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

actual object KmpAppLifecycleObserver : IKmpAppLifecycleObserver {

    private val _state = MutableStateFlow(KmpAppLifecycle.Active)
    private val scope = CoroutineScope(Dispatchers.Default)

    actual override val lifecycle: StateFlow<KmpAppLifecycle> = _state

    actual override fun init(context: KmpAppLifecycleObserverContext) {
        context.window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent?) {
                _state.tryEmit(KmpAppLifecycle.Active)
            }

            override fun windowLostFocus(e: WindowEvent?) {
                scope.launch {
                    delay(16) // Because both minimized and focus events fire when minimized, wait to see if minimized has fired
                    if (_state.value == KmpAppLifecycle.Background) return@launch
                    _state.tryEmit(KmpAppLifecycle.Inactive)
                }
            }
        })

        context.window.addWindowStateListener { e ->
            when {
                e.newState and Frame.ICONIFIED > 0 -> _state.tryEmit(KmpAppLifecycle.Background)
                else -> {
                    if (_state.value == KmpAppLifecycle.Inactive) return@addWindowStateListener
                    _state.tryEmit(KmpAppLifecycle.Active)
                }
            }
        }
    }
}

actual class KmpAppLifecycleObserverContext(val window: Window)