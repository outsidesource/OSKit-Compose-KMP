package com.outsidesource.oskitcompose.resources

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

actual data class KMPResource internal constructor(
    internal val resourceId: Int? = null,
    internal val path: String? = null,
) {
    constructor(resourceId: Int): this(resourceId = resourceId, path = null)
    constructor(path: String): this(resourceId = null, path = path)

    @OptIn(ExperimentalResourceApi::class)
    actual suspend fun readBytes(): ByteArray {
        return if (path != null) {
            resource(path).readBytes()
        } else {
            byteArrayOf()
        }
    }
}