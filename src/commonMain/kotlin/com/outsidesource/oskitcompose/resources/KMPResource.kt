package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@Deprecated("Use Compose Multiplatform 1.6.0 Resources instead")
@Stable
data class KMPResource(
    val path: String,
) {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(): ByteArray = resource(path).readBytes()
}