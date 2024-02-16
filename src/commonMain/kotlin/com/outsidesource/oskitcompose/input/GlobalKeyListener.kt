package com.outsidesource.oskitcompose.input

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

typealias GlobalKeyListenerCallback = (event: KeyEvent) -> Boolean

class GlobalKeyListener {
    companion object {
        private val globalKeyListeners = atomic(listOf<GlobalKeyListenerCallback>())

        fun add(listener: GlobalKeyListenerCallback) = globalKeyListeners.update { it + listener }
        fun remove(listener: GlobalKeyListenerCallback) = globalKeyListeners.update { it - listener }
        fun onEvent(event: KeyEvent) = globalKeyListeners.value.any { it(event) }
    }
}


