package com.outsidesource.oskitcompose.canvas

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import okio.BufferedSource

actual fun kmpLoadImageBitmap(source: BufferedSource): ImageBitmap = loadImageBitmap(source.inputStream())
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = loadSvgPainter(source.inputStream(), density)