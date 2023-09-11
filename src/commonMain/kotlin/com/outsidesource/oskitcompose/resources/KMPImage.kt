package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.math.ceil

@Stable
data class KMPImage(
    val path: String,
    val path2x: String? = null,
    val path3x: String? = null,
) {
    @Composable
    fun pathForDensity(): String {
        val density = ceil(LocalDensity.current.density)

        return when {
            density >= 3f -> path3x ?: path2x ?: path
            density == 2f -> path2x ?: path
            else -> path
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
expect fun rememberKmpImage(resource: KMPImage): Painter