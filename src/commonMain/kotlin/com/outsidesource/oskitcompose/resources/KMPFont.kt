package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@Deprecated("Use Compose Multiplatform 1.6.0 Resources instead")
data class KMPFontFamily(val fonts: List<KMPFont>)

@Stable
@Deprecated("Use Compose Multiplatform 1.6.0 Resources instead")
data class KMPFont(
    val path: String,
    val weight: FontWeight = FontWeight.Normal,
    val style: FontStyle = FontStyle.Normal,
) {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(): ByteArray = resource(path).readBytes()
}

/**
 * Remembers a font family from a KMPFontFamily. This blocks while the font is loaded into memory.
 * Use this sparingly. Use [resolveKmpFontFamily] and store it for use later instead.
 */
@Composable
@Deprecated("Use Compose Multiplatform 1.6.0 Resources instead")
expect fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily

/**
 * Resolves a KMPFontFamily to be used in Compose
 */
@Deprecated("Use Compose Multiplatform 1.6 Resources instead")
expect suspend fun resolveKmpFontFamily(family: KMPFontFamily): FontFamily