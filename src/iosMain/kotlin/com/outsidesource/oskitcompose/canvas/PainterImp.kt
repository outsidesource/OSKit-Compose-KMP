package com.outsidesource.oskitcompose.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import com.outsidesource.oskitcompose.resources.KMPResource
import okio.BufferedSource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Image

actual fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap = Image.makeFromEncoded(source.readByteArray()).toComposeImageBitmap()
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = BitmapPainter(ImageBitmap(0, 0))

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberKmpPainterResource(resource: KMPResource): Painter =
    painterResource(resource.pathForDensity())
