package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@Composable
actual fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily {
    return remember(family) {
        runBlocking { resolveKmpFontFamily(family) }
    }
}

@OptIn(ExperimentalResourceApi::class)
actual suspend fun resolveKmpFontFamily(family: KMPFontFamily): FontFamily {
    return FontFamily(family.fonts.map {
        val resource = resource((it.resource as KMPResource.iOS).path)
        val bytes = runBlocking { resource.readBytes() }

        Font(
            identity = it.resource.path,
            data = bytes,
            weight = it.weight,
            style = it.style,
        )
    })
}