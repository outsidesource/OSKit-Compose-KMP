package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import kotlinx.coroutines.runBlocking

@Composable
actual fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily {
    return remember(family) {
        runBlocking { resolveKmpFontFamily(family) }
    }
}

actual suspend fun resolveKmpFontFamily(family: KMPFontFamily): FontFamily {
    return FontFamily(family.fonts.map {
        Font(
            resource = (it.resource as KMPResource.Desktop).path,
            weight = it.weight,
            style = it.style,
        )
    })
}