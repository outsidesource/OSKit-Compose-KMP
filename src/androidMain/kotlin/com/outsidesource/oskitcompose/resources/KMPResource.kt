package com.outsidesource.oskitcompose.resources

actual sealed class KMPResource {
    data class Android(val id: Int) : KMPResource()
}