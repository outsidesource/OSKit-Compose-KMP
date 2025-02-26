package com.outsidesource.oskitcompose.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.IntSize
import kotlin.math.*

interface IKmpColorPickerRenderer {
    fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path
    fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions)
    fun colorForOffset(color: HsvColor, offset: Offset, size: IntSize): HsvColor
    fun offsetForColor(color: HsvColor, size: IntSize): Offset
}

@Immutable
data class KmpColorPickerRendererOptions(
    val renderAlpha: Boolean = true,
    val renderAlphaChecker: Boolean = true,
    val render3rdComponent: Boolean = true,
)

object KmpColorPickerRenderer {
    fun Sv() = SvColorPickerRenderer()
    fun Hv() = HvColorPickerRenderer()
    fun Hs() = HsColorPickerRenderer()
    fun HsCircle() = HsCircleColorPickerRenderer()
    fun HvCircle() = HvCircleColorPickerRenderer()
}

class SvColorPickerRenderer : IKmpColorPickerRenderer {
    private val path = Path()

    override fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path = path.apply {
        reset()
        addRect(size.toRect())
    }

    override fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions) {
        val fullHueColor = Color.hsv(color.hue, 1f, 1f)

        canvas.drawRect(
            paint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(size.width, 0f),
                    colors = listOf(Color.White, fullHueColor),
                )
            },
            rect = Rect(Offset(0f, 0f), size),
        )

        if (options.render3rdComponent) {
            canvas.drawRect(
                paint = Paint().apply {
                    blendMode = BlendMode.Multiply
                    shader = LinearGradientShader(
                        from = Offset(0f, 0f),
                        to = Offset(0f, size.height),
                        colors = listOf(Color.Transparent, Color.Black),
                    )
                },
                rect = Rect(Offset(0f, 0f), size),
            )
        }
    }

    override fun colorForOffset(
        color: HsvColor,
        offset: Offset,
        size: IntSize
    ): HsvColor {
        val saturation = ((100f / size.width) * offset.x).coerceIn(0f..100f) / 100f
        val value = (100f - ((100f / size.height) * offset.y).coerceIn(0f..100f)) / 100f
        return color.copy(saturation = saturation, value = value)
    }

    override fun offsetForColor(
        color: HsvColor,
        size: IntSize
    ): Offset {
        val x = (size.width * color.saturation).coerceIn(0f, size.width.toFloat())
        val y = (size.height * (1f - color.value)).coerceIn(0f, size.height.toFloat())
        return Offset(x, y)
    }
}

class HvColorPickerRenderer : IKmpColorPickerRenderer {
    private val path = Path()

    override fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path = path.apply {
        reset()
        addRect(size.toRect())
    }

    override fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions) {
        canvas.drawRect(
            paint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(size.width, 0f),
                    colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                )
            },
            rect = Rect(Offset(0f, 0f), size),
        )

        canvas.drawRect(
            paint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(0f, size.height),
                    colors = listOf(Color.Transparent, Color.Black),
                )
            },
            rect = Rect(Offset(0f, 0f), size),
        )

        if (options.render3rdComponent) {
            canvas.drawRect(
                paint = Paint().apply {
                    shader = LinearGradientShader(
                        from = Offset(0f, 0f),
                        to = Offset(0f, size.height),
                        colors = listOf(Color.White, Color.Black),
                    )
                    alpha = 1f - color.saturation
                },
                rect = Rect(Offset(0f, 0f), size),
            )
        }
    }

    override fun colorForOffset(
        color: HsvColor,
        offset: Offset,
        size: IntSize
    ): HsvColor {
        val hue = ((360f / size.width) * offset.x).coerceIn(0f..360f)
        val value = (100f - ((100f / size.height) * offset.y).coerceIn(0f..100f)) / 100f
        return color.copy(hue = hue, value = value)
    }

    override fun offsetForColor(
        color: HsvColor,
        size: IntSize
    ): Offset {
        val x = (size.width * (color.hue / 360f)).coerceIn(0f, size.width.toFloat())
        val y = (size.height * (1f - color.value)).coerceIn(0f, size.height.toFloat())
        return Offset(x, y)
    }
}

class HsColorPickerRenderer : IKmpColorPickerRenderer {
    private val path = Path()

    override fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path = path.apply {
        reset()
        addRect(size.toRect())
    }

    override fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions) {
        canvas.drawRect(
            paint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(size.width, 0f),
                    colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                )
            },
            rect = Rect(Offset(0f, 0f), size),
        )

        canvas.drawRect(
            paint = Paint().apply {
                shader = LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(0f, size.height),
                    colors = listOf(Color.White, Color.Transparent),
                )
            },
            rect = Rect(Offset(0f, 0f), size),
        )

        if (options.render3rdComponent) {
            canvas.drawRect(
                paint = Paint().apply {
                    this.color = Color.Black
                    alpha = 1f - color.value
                },
                rect = Rect(Offset(0f, 0f), size),
            )
        }
    }

    override fun colorForOffset(
        color: HsvColor,
        offset: Offset,
        size: IntSize
    ): HsvColor {
        val hue = ((360f / size.width) * offset.x).coerceIn(0f..360f)
        val saturation = ((100f / size.height) * offset.y).coerceIn(0f..100f) / 100f
        return color.copy(hue = hue, saturation = saturation)
    }

    override fun offsetForColor(
        color: HsvColor,
        size: IntSize
    ): Offset {
        val x = (size.width * (color.hue / 360f)).coerceIn(0f, size.width.toFloat())
        val y = (size.height * color.saturation).coerceIn(0f, size.height.toFloat())
        return Offset(x, y)
    }
}

class HsCircleColorPickerRenderer : IKmpColorPickerRenderer {
    private val path = Path()

    override fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path = path.apply {
        reset()
        addArc(size.toRect(), 0f, 360f)
    }

    override fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions) {
        val radius = min(size.width, size.height) / 2f

        canvas.save()
        canvas.rotate(90f, size.center.x, size.center.y)

        canvas.drawCircle(
            center = size.center,
            paint = Paint().apply {
                shader = SweepGradientShader(
                    center = size.center,
                    colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                )
            },
            radius = radius,
        )

        canvas.drawCircle(
            center = size.center,
            paint = Paint().apply {
                shader = RadialGradientShader(
                    center = size.center,
                    radius = radius,
                    colors = listOf(Color.White, Color.Transparent),
                )
            },
            radius = radius,
        )

        if (options.render3rdComponent) {
            canvas.drawCircle(
                center = size.center,
                paint = Paint().apply {
                    this.color = Color.Black
                    alpha = 1f - color.value
                },
                radius = radius,
            )
        }

        canvas.restore()
    }

    override fun colorForOffset(
        color: HsvColor,
        offset: Offset,
        size: IntSize
    ): HsvColor {
        val centerX: Double = size.width / 2.0
        val centerY: Double = size.height / 2.0
        val circleRadius: Double = min(centerX, centerY)
        val xOffset: Double = offset.x - centerX
        val yOffset: Double = offset.y - centerY
        val radius = hypot(xOffset, yOffset)
        val rawAngle = atan2(yOffset, xOffset).toDegree() - 90f
        val normalizedAngle = (rawAngle + 360.0) % 360.0
        return color.copy(hue = normalizedAngle.toFloat(), saturation = (radius / circleRadius).toFloat().coerceIn(0f, 1f))
    }

    override fun offsetForColor(
        color: HsvColor,
        size: IntSize
    ): Offset {
        val circleRadius = min(size.height, size.width) / 2f
        val theta = (color.hue + 90f).toRadians()
        val polarRadius = circleRadius * color.saturation
        val x = polarRadius * cos(theta)
        val y = polarRadius * sin(theta)
        return Offset((size.width / 2f) + x.toFloat(), (size.height / 2f) + y.toFloat())
    }
}

class HvCircleColorPickerRenderer : IKmpColorPickerRenderer {
    private val path = Path()

    override fun clipPath(color: HsvColor, size: Size, options: KmpColorPickerRendererOptions): Path = path.apply {
        reset()
        addArc(size.toRect(), 0f, 360f)
    }

    override fun draw(color: HsvColor, canvas: Canvas, size: Size, options: KmpColorPickerRendererOptions) {
        val radius = min(size.width, size.height) / 2f

        canvas.save()
        canvas.rotate(90f, size.center.x, size.center.y)

        canvas.drawCircle(
            center = size.center,
            paint = Paint().apply {
                shader = SweepGradientShader(
                    center = size.center,
                    colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                )
            },
            radius = radius,
        )

        canvas.drawCircle(
            center = size.center,
            paint = Paint().apply {
                shader = RadialGradientShader(
                    center = size.center,
                    radius = radius,
                    colors = listOf(Color.Black, Color.Transparent),
                )
            },
            radius = radius,
        )

        // Clip path to fix anti-aliasing halo around saturation
        canvas.clipPath(Path().apply { addArc(size.toRect().inflate(1f), 0f, 360f) })

        if (options.render3rdComponent) {
            canvas.drawRect(
                rect = size.toRect(),
                paint = Paint().apply {
                    shader = RadialGradientShader(
                        center = size.center,
                        radius = radius,
                        colors = listOf(Color.Black, Color.White),
                    )
                    alpha = 1f - color.saturation
                },
            )
        }

        canvas.restore()
    }

    override fun colorForOffset(
        color: HsvColor,
        offset: Offset,
        size: IntSize
    ): HsvColor {
        val centerX: Double = size.width / 2.0
        val centerY: Double = size.height / 2.0
        val circleRadius: Double = min(centerX, centerY)
        val cartesianX: Double = offset.x - centerX
        val cartesianY: Double = offset.y - centerY
        val radius = hypot(cartesianX, cartesianY)
        val rawAngle = atan2(cartesianY, cartesianX).toDegree() - 90f
        val normalizedAngle = (rawAngle + 360.0) % 360.0
        return color.copy(hue = normalizedAngle.toFloat(), value = (radius / circleRadius).toFloat().coerceIn(0f, 1f))
    }

    override fun offsetForColor(
        color: HsvColor,
        size: IntSize
    ): Offset {
        val circleRadius = min(size.height, size.width) / 2f
        val theta = (color.hue + 90f).toRadians()
        val polarRadius = circleRadius * color.value
        val x = polarRadius * cos(theta)
        val y = polarRadius * sin(theta)
        return Offset((size.width / 2f) + x.toFloat(), (size.height / 2f) + y.toFloat())
    }
}

fun Double.toDegree(): Double = this * 180.0 / PI.toDouble()
fun Float.toRadians(): Double = this * PI.toDouble() / 180.0