package com.outsidesource.oskitcompose.canvas

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import java.io.InputStream

actual fun kmpLoadImageBitmap(input: InputStream) = BitmapFactory.decodeStream(input).asImageBitmap()
actual fun kmpLoadSvgPainter(input: InputStream, density: Density): Painter = BitmapPainter(ImageBitmap(0, 0))