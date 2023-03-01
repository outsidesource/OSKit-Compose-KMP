package com.outsidesource.oskitcompose.input

import androidx.compose.ui.input.key.KeyEvent

typealias GlobalKeyListenerCallback = (event: KeyEvent) -> Boolean

class GlobalKeyListener {
    companion object {
        private val globalKeyListeners = mutableListOf<GlobalKeyListenerCallback>()

        fun add(listener: GlobalKeyListenerCallback) = globalKeyListeners.add(listener)
        fun remove(listener: GlobalKeyListenerCallback) = globalKeyListeners.remove(listener)
        fun onEvent(event: KeyEvent) = globalKeyListeners.any { it(event) }
    }
}


