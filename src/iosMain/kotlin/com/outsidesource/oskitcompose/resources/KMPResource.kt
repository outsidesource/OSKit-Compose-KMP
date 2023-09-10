package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import kotlin.math.ceil

actual class KMPResource(
    internal val path: String,
    internal val path2x: String? = null,
    internal val path3x: String? = null,
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

    @OptIn(ExperimentalResourceApi::class)
    actual suspend fun readBytes(): ByteArray = resource(path).readBytes()
}