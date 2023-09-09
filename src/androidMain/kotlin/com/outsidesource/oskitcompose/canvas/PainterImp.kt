package com.outsidesource.oskitcompose.canvas

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import com.outsidesource.oskitcompose.resources.KMPResource
import okio.BufferedSource

actual fun kmpLoadImageBitmap(source: BufferedSource) = BitmapFactory.decodeStream(source.inputStream()).asImageBitmap()
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = BitmapPainter(ImageBitmap(0, 0))

@Composable
actual fun rememberKmpPainterResource(resource: KMPResource): Painter =
    painterResource(id = (resource as KMPResource.Android).id)
