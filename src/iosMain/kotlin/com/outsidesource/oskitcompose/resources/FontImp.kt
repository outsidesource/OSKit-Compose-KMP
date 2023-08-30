package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer

@Composable
actual fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily {
    return remember(family) {
        FontFamily(family.fonts.map {
            val source = FileSystem.SYSTEM.source((it.resource as KMPResource.iOS).path.toPath())
            val bytes = source.buffer().readByteArray()
            Font(
                identity = it.resource.path,
                data = bytes,
                weight = it.weight,
                style = it.style,
            )
        })
    }
}