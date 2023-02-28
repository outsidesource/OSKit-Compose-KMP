package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

data class KMPFontFamily(val fonts: List<KMPFont>)

data class KMPFont(
    val resource: KMPResource,
    val weight: FontWeight = FontWeight.Normal,
    val style: FontStyle = FontStyle.Normal,
)

@Composable
expect fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily
