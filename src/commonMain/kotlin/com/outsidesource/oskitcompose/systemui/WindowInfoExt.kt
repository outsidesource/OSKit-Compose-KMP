package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    XS,
    S,
    M,
    L,
    XL,
    ;

    companion object {
        val SmallPhone = XS
        val Phone = S
        val Tablet = M
        val LargeTablet = L
        val Desktop = XL
    }
}

@get:Composable
val WindowInfo.widthSizeClass : WindowSizeClass
    get() {
        val width = with(LocalDensity.current) { containerSize.width.toDp() }
        return when {
            width < 480.dp -> WindowSizeClass.XS
            width < 600.dp -> WindowSizeClass.S
            width < 840.dp -> WindowSizeClass.M
            width < 1200.dp -> WindowSizeClass.L
            else -> WindowSizeClass.XL
        }
    }

@get:Composable
val WindowInfo.containerSizeDp : DpSize
    get() = with(LocalDensity.current) { DpSize(containerSize.width.toDp(), containerSize.height.toDp()) }