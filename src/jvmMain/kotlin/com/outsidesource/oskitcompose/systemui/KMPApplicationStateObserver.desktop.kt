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

actual object KMPApplicationStateObserver : IKMPApplicationStateObserver {

    private var hasInitialized: Boolean = false
    private val _state = MutableStateFlow(KMPApplicationState.Active)
    private val scope = CoroutineScope(Dispatchers.Default)

    override val state: StateFlow<KMPApplicationState> = _state

    override fun init(context: KMPApplicationStateObserverContext) {
        context.window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent?) {
                _state.tryEmit(KMPApplicationState.Active)
            }

            override fun windowLostFocus(e: WindowEvent?) {
                scope.launch {
                    delay(16) // Because both minimized and focus events fire when minimized, wait to see if minimized has fired
                    if (_state.value == KMPApplicationState.Background) return@launch
                    _state.tryEmit(KMPApplicationState.Inactive)
                }
            }
        })

        context.window.addWindowStateListener { e ->
            when {
                e.newState and Frame.ICONIFIED > 0 -> _state.tryEmit(KMPApplicationState.Background)
                else -> {
                    if (_state.value == KMPApplicationState.Inactive) return@addWindowStateListener
                    _state.tryEmit(KMPApplicationState.Active)
                }
            }
        }
    }
}

actual class KMPApplicationStateObserverContext(val window: Window)