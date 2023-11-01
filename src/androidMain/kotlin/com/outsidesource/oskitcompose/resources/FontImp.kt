package com.outsidesource.oskitcompose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.io.File

@Composable
actual fun rememberKmpFontFamily(family: KMPFontFamily): FontFamily {
    return remember(family) {
        runBlocking { resolveKmpFontFamily(family) }
    }
}

@OptIn(ExperimentalResourceApi::class)
actual suspend fun resolveKmpFontFamily(family: KMPFontFamily): FontFamily {
    return FontFamily(family.fonts.map {
        val name = it.path.toPath().name
        val file = File.createTempFile(name, null)
        val os = file.outputStream()
        val bytes = resource(it.path).readBytes()

        os.write(bytes)
        os.flush()
        os.close()

        Font(
            file = file,
            weight = it.weight,
            style = it.style,
        )
    })
}