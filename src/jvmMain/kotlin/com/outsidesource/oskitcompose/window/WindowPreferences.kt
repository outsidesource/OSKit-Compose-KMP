package com.outsidesource.oskitcompose.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import java.util.prefs.Preferences

object WindowPrefs {
    const val WindowPosX = "windowPosX"
    const val WindowPosY = "windowPosY"
    const val WindowSizeW = "windowSizeW"
    const val WindowSizeH = "windowSizeH"
}

@Composable
fun rememberPersistedWindowState(node: String): WindowState {
    val prefs = remember { Preferences.userRoot().node(node) }

    val windowSize = remember {
        DpSize(
            width = prefs.getFloat(WindowPrefs.WindowSizeW, 1600f).dp,
            height = prefs.getFloat(WindowPrefs.WindowSizeH, 1000f).dp,
        )
    }
    val windowPosition = remember {
        WindowPosition(
            x = prefs.getFloat(WindowPrefs.WindowPosX, 0f).dp,
            y = prefs.getFloat(WindowPrefs.WindowPosY, 0f).dp,
        )
    }
    val windowState = rememberWindowState(position = windowPosition, size = windowSize)

    LaunchedEffect(windowState.position) {
        delay(250)
        prefs.putFloat(WindowPrefs.WindowPosX, windowState.position.x.value)
        prefs.putFloat(WindowPrefs.WindowPosY, windowState.position.y.value)
        prefs.flush()
    }

    LaunchedEffect(windowState.size) {
        delay(250)
        prefs.putFloat(WindowPrefs.WindowSizeW, windowState.size.width.value)
        prefs.putFloat(WindowPrefs.WindowSizeH, windowState.size.height.value)
        prefs.flush()
    }

    return windowState
}