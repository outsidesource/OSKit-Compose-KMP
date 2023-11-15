package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import kotlin.math.ceil

@Stable
data class KMPImage(
    val path: String,
    val path2x: String? = null,
    val path3x: String? = null,
) {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(density: Density): ByteArray {
        val path = internalPathForDensity(density)
        return resource(path).readBytes()
    }

    @Composable
    fun pathForDensity(): String {
        val density = LocalDensity.current
        return internalPathForDensity(density)
    }

    private fun internalPathForDensity(density: Density): String {
        val scale = ceil(density.density)

        return when {
            scale >= 3f -> path3x ?: path2x ?: path
            scale == 2f -> path2x ?: path
            else -> path
        }
    }
}

@Composable
expect fun rememberKmpImagePainter(resource: KMPImage): Painter