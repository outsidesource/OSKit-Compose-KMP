package com.outsidesource.oskitcompose.canvas

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import okio.BufferedSource

actual fun kmpLoadImageBitmap(source: BufferedSource) = BitmapFactory.decodeStream(source.inputStream()).asImageBitmap()
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = ImageLoadErrorPainter