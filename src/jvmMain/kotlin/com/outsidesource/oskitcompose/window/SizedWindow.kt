package com.outsidesource.oskitcompose.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.*
import com.outsidesource.oskitcompose.lib.VarRef
import java.awt.Dimension
import java.awt.event.WindowEvent
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SizedWindow(
    minWindowSize: Dimension = Dimension(1280, 768),
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "",
    icon: androidx.compose.ui.graphics.painter.Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean = { false },
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean = { false },
    content: @Composable (FrameWindowScope.() -> Unit),
) {
    CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides composeWindowExceptionHandler) {
        Window(
            onCloseRequest = onCloseRequest,
            state = state,
            visible = visible,
            title = title,
            icon = icon,
            undecorated = undecorated,
            transparent = transparent,
            resizable = resizable,
            enabled = enabled,
            focusable = focusable,
            alwaysOnTop = alwaysOnTop,
            onPreviewKeyEvent = onPreviewKeyEvent,
            onKeyEvent = onKeyEvent,
        ) {
            val density = LocalDensity.current.density
            val shouldResetMinimumWindowSize = remember(density) { VarRef(true) }
            val minWindowSizeState = remember(density, minWindowSize) { minWindowSize }

            if (shouldResetMinimumWindowSize.value) {
                window.minimumSize = minWindowSizeState
                shouldResetMinimumWindowSize.value = false
            }

            CompositionLocalProvider(LocalWindowState provides state, LocalWindow provides window) {
                content()
            }
        }
    }
}

val LocalWindowState = staticCompositionLocalOf { WindowState() }
val LocalWindow = staticCompositionLocalOf { ComposeWindow() }

@OptIn(ExperimentalComposeUiApi::class)
private val composeWindowExceptionHandler = WindowExceptionHandlerFactory { window ->
    WindowExceptionHandler { throwable ->
        // invokeLater here to dispatch a blocking operation (showMessageDialog) and throw the exception after immediately
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(window, "An unknown error has occurred", "Error", JOptionPane.ERROR_MESSAGE)
            window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
        }
        throw throwable
    }
}