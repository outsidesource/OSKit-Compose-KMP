package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.InputStream

actual fun kmpLoadImageBitmap(input: InputStream): ImageBitmap = loadImageBitmap(input)
actual fun kmpLoadSvgPainter(input: InputStream, density: Density): Painter = loadSvgPainter(input, density)