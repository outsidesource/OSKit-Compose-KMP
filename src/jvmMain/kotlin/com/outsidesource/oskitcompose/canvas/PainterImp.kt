package com.outsidesource.oskitcompose.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import com.outsidesource.oskitcompose.resources.KMPResource
import java.io.InputStream

actual fun kmpLoadImageBitmap(input: InputStream): ImageBitmap = loadImageBitmap(input)
actual fun kmpLoadSvgPainter(input: InputStream, density: Density): Painter = loadSvgPainter(input, density)

@Composable
actual fun rememberKmpPainterResource(resource: KMPResource): Painter = painterResource((resource as KMPResource.Desktop).path)
