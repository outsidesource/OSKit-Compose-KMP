package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Stable

@Stable
expect class KMPResource {
    /**
     * Returns the bytes from the resource file.
     * Note: Android KMPResources that use resource Ids (Int) will return an empty byte array
     */
    suspend fun readBytes(): ByteArray
}