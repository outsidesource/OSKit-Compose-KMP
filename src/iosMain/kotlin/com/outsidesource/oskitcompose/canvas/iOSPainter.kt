package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import okio.BufferedSource
import org.jetbrains.skia.Image

actual fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap = Image.makeFromEncoded(source.readByteArray()).toComposeImageBitmap()
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = ImageLoadErrorPainter(density)