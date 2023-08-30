package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.ceil

actual sealed class KMPResource {
    data class iOS(
        val path: String,
        val path2x: String? = null,
        val path3x: String? = null,
    ) : KMPResource() {

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
}