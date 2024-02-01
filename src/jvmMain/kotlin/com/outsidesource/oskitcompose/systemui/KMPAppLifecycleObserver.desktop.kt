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

actual object KMPAppLifecycleObserver : IKMPAppLifecycleObserver {

    private val _state = MutableStateFlow(KMPAppLifecycle.Active)
    private val scope = CoroutineScope(Dispatchers.Default)

    override val lifecycle: StateFlow<KMPAppLifecycle> = _state

    override fun init(context: KMPAppLifecycleObserverContext) {
        context.window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent?) {
                _state.tryEmit(KMPAppLifecycle.Active)
            }

            override fun windowLostFocus(e: WindowEvent?) {
                scope.launch {
                    delay(16) // Because both minimized and focus events fire when minimized, wait to see if minimized has fired
                    if (_state.value == KMPAppLifecycle.Background) return@launch
                    _state.tryEmit(KMPAppLifecycle.Inactive)
                }
            }
        })

        context.window.addWindowStateListener { e ->
            when {
                e.newState and Frame.ICONIFIED > 0 -> _state.tryEmit(KMPAppLifecycle.Background)
                else -> {
                    if (_state.value == KMPAppLifecycle.Inactive) return@addWindowStateListener
                    _state.tryEmit(KMPAppLifecycle.Active)
                }
            }
        }
    }
}

actual class KMPAppLifecycleObserverContext(val window: Window)