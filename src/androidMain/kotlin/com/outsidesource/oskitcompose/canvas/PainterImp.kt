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
import org.jetbrains.compose.resources.ExperimentalResourceApi

actual fun kmpLoadImageBitmap(source: BufferedSource) = BitmapFactory.decodeStream(source.inputStream()).asImageBitmap()
actual fun kmpLoadSvgPainter(source: BufferedSource, density: Density): Painter = BitmapPainter(ImageBitmap(0, 0))

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberKmpPainterResource(resource: KMPResource): Painter {
    return if (resource.resourceId != null) {
        painterResource(id = resource.resourceId)
    } else if (resource.path != null) {
        org.jetbrains.compose.resources.painterResource(res = resource.path)
    } else {
        // This cannot happen due to the KMPResource constructors
        throw Exception("Invalid KMPResource")
    }
}
