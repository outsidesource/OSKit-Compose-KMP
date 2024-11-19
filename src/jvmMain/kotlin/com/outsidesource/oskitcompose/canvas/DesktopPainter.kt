package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import okio.BufferedSource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.skia.Image
import org.jetbrains.skia.Image.Companion

actual fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap =
    Image.makeFromEncoded(source.readByteArray()).toComposeImageBitmap()

@OptIn(ExperimentalResourceApi::class)
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = source.readByteArray()
    .decodeToSvgPainter(density)