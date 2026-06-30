package com.example.skycast.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val y: Float,
    val speed: Float, val size: Float,
    val alpha: Float, val angle: Float,
    val phase: Float,
    val isSnowflakeIcon: Boolean = false,
)

@Composable
fun WeatherParticles(
    weatherType: WeatherType,
    isNight: Boolean,
    modifier: Modifier = Modifier,
) {
    val loopMs = loopDuration(weatherType)
    val count = when (weatherType) {
        WeatherType.RAINY, WeatherType.THUNDERSTORM -> 40
        WeatherType.SNOWY -> 24
        else -> 0
    }

    var elapsedCycles by remember { mutableStateOf(0f) }

    LaunchedEffect(weatherType) {
        elapsedCycles = 0f
        while (isActive) {
            kotlinx.coroutines.delay(16)
            elapsedCycles += 16f / loopMs
        }
    }

    if (count == 0) return

    val particleColor = particleColor(weatherType, isNight)

    val particles = remember(weatherType) {
        List(count) { index ->
            val isIcon = weatherType == WeatherType.SNOWY && index >= count * 2 / 3
            val sz = when (weatherType) {
                WeatherType.RAINY, WeatherType.THUNDERSTORM -> 0.008f + Random.nextFloat() * 0.012f
                WeatherType.SNOWY -> {
                    val tier = Random.nextFloat()
                    if (tier < 0.55f) 0.008f + Random.nextFloat() * 0.008f
                    else 0.016f + Random.nextFloat() * 0.014f
                }
                else -> 0f // unreachable
            }
            Particle(
                x = Random.nextFloat(), y = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.6f,
                size = sz,
                alpha = 0.3f + Random.nextFloat() * 0.7f,
                angle = if (weatherType == WeatherType.SNOWY) Random.nextFloat() * 30f - 15f
                        else Random.nextFloat() * 15f - 7.5f,
                phase = Random.nextFloat(),
                isSnowflakeIcon = isIcon,
            )
        }
    }

    Canvas(modifier = modifier) {
        val w = this.size.width
        val h = this.size.height
        val t = elapsedCycles

        particles.forEach { p ->
            val rawY = (p.y + t * p.speed) % 1.15f - 0.15f
            val y = rawY * h
            if (y < -10f || y > h + 10f) return@forEach

            val alpha = when {
                rawY < 0f -> p.alpha * (1f + rawY / 0.15f)
                rawY > 0.85f -> p.alpha * ((1f - rawY) / 0.15f)
                else -> p.alpha
            }

            val px = when {
                weatherType == WeatherType.SNOWY -> {
                    val sway = sin((t * 4f + p.phase * 6.28f)).toFloat() * w * 0.04f
                    (p.x * w + sway).coerceIn(0f, w)
                }
                else -> {
                    val rad = Math.toRadians(p.angle.toDouble())
                    val drift = (rawY * h * 0.12f * sin(rad)).toFloat()
                    (p.x * w + drift).coerceIn(0f, w)
                }
            }

            val c = particleColor.copy(alpha = alpha.coerceIn(0f, 1f))

            when {
                weatherType == WeatherType.SNOWY && p.isSnowflakeIcon -> {
                    val r = p.size * w * 2.5f
                    drawFractalSnowflake(c, Offset(px, y), r)
                }
                weatherType == WeatherType.SNOWY -> {
                    val r = p.size * w * 1.5f
                    drawFractalSnowflake(c, Offset(px, y), r)
                }
                else -> {
                    val len = p.size * w * 3f
                    val rad = Math.toRadians(p.angle.toDouble())
                    drawLine(c, Offset(px, y),
                        Offset((px + len * cos(rad + Math.PI / 2)).toFloat(),
                               (y + len * sin(rad + Math.PI / 2)).toFloat()),
                        strokeWidth = p.size * w * 0.5f)
                }
            }
        }
    }
}

/** 分形雪花：6臂，每股2侧枝 */
private fun DrawScope.drawFractalSnowflake(color: Color, center: Offset, r: Float) {
    for (i in 0 until 6) {
        val angle = Math.toRadians(i * 60.0)
        val cs = cos(angle).toFloat()
        val sn = sin(angle).toFloat()
        val tipX = center.x + r * cs
        val tipY = center.y + r * sn
        drawLine(color, center, Offset(tipX, tipY), strokeWidth = r * 0.16f)
        listOf(0.55f, 0.80f).forEach { frac ->
            val bx = center.x + r * frac * cs
            val by = center.y + r * frac * sn
            val br = r * 0.28f
            listOf(angle + Math.toRadians(55.0), angle - Math.toRadians(55.0)).forEach { ba ->
                drawLine(color, Offset(bx, by),
                    Offset(bx + br * cos(ba).toFloat(), by + br * sin(ba).toFloat()),
                    strokeWidth = r * 0.10f)
            }
        }
    }
}

private fun loopDuration(type: WeatherType): Int = when (type) {
    WeatherType.RAINY, WeatherType.THUNDERSTORM -> 1500
    WeatherType.SNOWY -> 4000
    else -> 3000
}

private fun particleColor(type: WeatherType, isNight: Boolean): Color = when {
    isNight -> Color.White.copy(alpha = 0.5f)
    type == WeatherType.RAINY || type == WeatherType.THUNDERSTORM -> Color(0xFFA0C8F0)
    type == WeatherType.SNOWY -> Color.White
    else -> Color.White
}
