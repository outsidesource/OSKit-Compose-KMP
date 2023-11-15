package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

@Composable
actual fun rememberKmpImagePainter(resource: KMPImage): Painter = painterResource(resource.pathForDensity())