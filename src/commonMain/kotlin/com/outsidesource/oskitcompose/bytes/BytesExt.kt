package com.outsidesource.oskitcompose.bytes

fun String.decodeHex(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.encodeToHex(): String = joinToString(separator = "") {
    it.toUByte().toString(16).padStart(2, '0')
}

fun UInt.reverse(): UInt {
    var reversed = 0u
    var input = this

    for (i in 0 until UInt.SIZE_BITS) {
        reversed = reversed shl 1
        if (input and 1u == 1u) reversed = reversed xor 1u
        input = input shr 1
    }

    return reversed
}