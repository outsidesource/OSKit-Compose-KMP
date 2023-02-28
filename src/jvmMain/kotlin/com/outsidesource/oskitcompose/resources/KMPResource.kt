package com.outsidesource.oskitcompose.resources

actual sealed class KMPResource {
    data class Desktop(val path: String) : KMPResource()
}