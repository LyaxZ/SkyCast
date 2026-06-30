package com.example.skycast.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class WeatherType {
    SUNNY, CLOUDY, OVERCAST,
    RAINY, SNOWY, THUNDERSTORM,
    FOGGY,
}

internal fun classify(weatherText: String): WeatherType {
    val t = weatherText.trim()
    return when {
        t.contains("雷") || t.contains("暴") -> WeatherType.THUNDERSTORM
        t.contains("雪") -> WeatherType.SNOWY
        t.contains("雨") || t.contains("阵") -> WeatherType.RAINY
        t.contains("雾") || t.contains("霾") || t.contains("沙") || t.contains("尘") -> WeatherType.FOGGY
        t == "晴" -> WeatherType.SUNNY
        t == "少云" || t == "多云" -> WeatherType.CLOUDY
        t == "阴" -> WeatherType.OVERCAST
        else -> WeatherType.CLOUDY
    }
}

@Composable
fun WeatherIcon(
    weatherText: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    tint: Color = Color.White,
) {
    val type = classify(weatherText)
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val margin = w * 0.06f
        val aw = w - margin * 2
        val ah = h - margin * 2

        when (type) {
            WeatherType.SUNNY -> drawSunny(tint, margin, aw, ah)
            WeatherType.CLOUDY -> drawCloudy(tint, margin, aw, ah)
            WeatherType.OVERCAST -> drawOvercast(tint, margin, aw, ah)
            WeatherType.RAINY -> drawRainy(tint, margin, aw, ah)
            WeatherType.SNOWY -> drawSnowy(tint, margin, aw, ah)
            WeatherType.THUNDERSTORM -> drawThunderstorm(tint, margin, aw, ah)
            WeatherType.FOGGY -> drawFoggy(tint, margin, aw, ah)
        }
    }
}

// ─── 晴（钝角三角形射线，与圆分离，不连在一起） ───
private fun DrawScope.drawSunny(c: Color, m: Float, aw: Float, ah: Float) {
    val cx = m + aw * 0.5f
    val cy = m + ah * 0.45f
    val r = aw * 0.15f
    val gap = r * 0.13f            // 三角形底边离圆周的间距
    val triH = r * 0.55f           // 三角形高度（钝三角，不高）
    val triHW = r * 0.22f          // 三角形底半宽（宽钝三角）
    drawCircle(c, r, Offset(cx, cy))
    for (i in 0 until 8) {
        val angle = Math.toRadians(i * 45.0)
        val cs = cos(angle).toFloat()
        val sn = sin(angle).toFloat()
        // 三角形底边中心在 gap 处
        val baseCx = cx + (r + gap) * cs
        val baseCy = cy + (r + gap) * sn
        // 钝三角形的三个顶点
        val tipX = cx + (r + gap + triH) * cs
        val tipY = cy + (r + gap + triH) * sn
        val px = -sn * triHW
        val py = cs * triHW
        val path = Path().apply {
            moveTo(baseCx + px, baseCy + py)
            lineTo(tipX, tipY)
            lineTo(baseCx - px, baseCy - py)
            close()
        }
        drawPath(path, c)
    }
}

// ─── 多云（太阳 + 两层云重叠） ──────────────────
private fun DrawScope.drawCloudy(c: Color, m: Float, aw: Float, ah: Float) {
    drawSunny(c.copy(alpha = 0.75f), m, aw, ah)
    drawCloud(c, m + aw * 0.60f, m + ah * 0.52f, aw * 0.22f, ah * 0.14f)
    drawCloud(c.copy(alpha = 0.85f), m + aw * 0.38f, m + ah * 0.58f, aw * 0.22f, ah * 0.14f)
}

// ─── 阴（大云 + 小云在右下角） ──────────────────
private fun DrawScope.drawOvercast(c: Color, m: Float, aw: Float, ah: Float) {
    drawCloud(c, m + aw * 0.45f, m + ah * 0.42f, aw * 0.40f, ah * 0.22f)
    drawCloud(c.copy(alpha = 0.8f), m + aw * 0.70f, m + ah * 0.60f, aw * 0.26f, ah * 0.16f)
}

// ─── 雨 ─────────────────────────────────────────
private fun DrawScope.drawRainy(c: Color, m: Float, aw: Float, ah: Float) {
    drawCloud(c, m + aw * 0.50f, m + ah * 0.28f, aw * 0.34f, ah * 0.18f)
    val rainColor = c.copy(alpha = 0.7f)
    val startY = m + ah * 0.52f
    val dropLen = ah * 0.13f
    listOf(aw * 0.28f, aw * 0.42f, aw * 0.56f, aw * 0.70f).forEach { x ->
        drawLine(rainColor, Offset(m + x, startY),
            Offset(m + x - aw * 0.03f, startY + dropLen), strokeWidth = 1.6.dp.toPx())
    }
}

// ─── 雷阵雨 ────────────────────────────────────
private fun DrawScope.drawThunderstorm(c: Color, m: Float, aw: Float, ah: Float) {
    drawCloud(c.copy(alpha = 0.85f), m + aw * 0.48f, m + ah * 0.22f, aw * 0.34f, ah * 0.18f)
    val rainColor = c.copy(alpha = 0.7f)
    val rainY = m + ah * 0.44f
    val rainLen = ah * 0.12f
    listOf(aw * 0.28f, aw * 0.42f, aw * 0.56f, aw * 0.70f).forEach { x ->
        drawLine(rainColor, Offset(m + x, rainY),
            Offset(m + x - aw * 0.03f, rainY + rainLen), strokeWidth = 1.6.dp.toPx())
    }
    val boltX = m + aw * 0.42f
    val boltTop = rainY + rainLen + ah * 0.03f
    val boltPath = Path().apply {
        moveTo(boltX + aw * 0.04f, boltTop)
        lineTo(boltX - aw * 0.05f, boltTop + ah * 0.10f)
        lineTo(boltX + aw * 0.02f, boltTop + ah * 0.10f)
        lineTo(boltX - aw * 0.04f, boltTop + ah * 0.22f)
        lineTo(boltX + aw * 0.08f, boltTop + ah * 0.06f)
        lineTo(boltX, boltTop + ah * 0.06f)
        close()
    }
    drawPath(boltPath, c)
}

// ─── 雪（云 + 下方雪花居中）───────────────────
private fun DrawScope.drawSnowy(c: Color, m: Float, aw: Float, ah: Float) {
    drawCloud(c, m + aw * 0.50f, m + ah * 0.28f, aw * 0.34f, ah * 0.18f)
    val snowColor = c.copy(alpha = 0.85f)
    val r = aw * 0.035f
    // 下方雪花右移居中
    listOf(
        Offset(m + aw * 0.34f, m + ah * 0.54f),
        Offset(m + aw * 0.56f, m + ah * 0.58f),
        Offset(m + aw * 0.72f, m + ah * 0.52f),
        Offset(m + aw * 0.44f, m + ah * 0.66f),
        Offset(m + aw * 0.64f, m + ah * 0.63f),
    ).forEach { p -> drawSnowflake(snowColor, p, r) }
}

// ─── 雾 ────────────────────────────────────────
private fun DrawScope.drawFoggy(c: Color, m: Float, aw: Float, ah: Float) {
    val fogColor = c.copy(alpha = 0.5f)
    val lineYs = listOf(m + ah * 0.38f, m + ah * 0.54f, m + ah * 0.70f)
    lineYs.forEach { baseY ->
        val path = Path().apply {
            val left = m + aw * 0.05f
            val right = m + aw * 0.95f
            val amp = ah * 0.04f
            val segW = aw / 6f
            moveTo(left, baseY)
            var x = left
            var sign = 1f
            while (x < right) {
                val endX = (x + segW).coerceAtMost(right)
                cubicTo(x + segW * 0.5f, baseY - amp * sign, x + segW * 0.5f, baseY - amp * sign, endX, baseY)
                x = endX
                sign = -sign
            }
        }
        drawPath(path, fogColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
    }
}

// ─── 分形雪花（6臂，每股2侧枝） ──────────────────
private fun DrawScope.drawSnowflake(color: Color, center: Offset, r: Float) {
    for (i in 0 until 6) {
        val angle = Math.toRadians(i * 60.0)
        val cs = cos(angle).toFloat()
        val sn = sin(angle).toFloat()
        val tipX = center.x + r * cs
        val tipY = center.y + r * sn
        drawLine(color, center, Offset(tipX, tipY), strokeWidth = r * 0.15f)
        listOf(0.55f, 0.80f).forEach { frac ->
            val bx = center.x + r * frac * cs
            val by = center.y + r * frac * sn
            val br = r * 0.25f
            listOf(angle + Math.toRadians(55.0), angle - Math.toRadians(55.0)).forEach { ba ->
                drawLine(color, Offset(bx, by),
                    Offset(bx + br * cos(ba).toFloat(), by + br * sin(ba).toFloat()),
                    strokeWidth = r * 0.10f)
            }
        }
    }
}

// ─── 云朵形状 ───────────────────────────────────
private fun DrawScope.drawCloud(color: Color, cx: Float, cy: Float, bw: Float, bh: Float) {
    val rectH = bh * 0.8f
    drawRect(color,
        topLeft = Offset(cx - bw * 0.6f, cy - rectH * 0.2f),
        size = Size(bw * 1.2f, rectH))
    drawCircle(color, bw * 0.42f, Offset(cx - bw * 0.15f, cy - rectH * 0.1f))
    drawCircle(color, bw * 0.50f, Offset(cx + bw * 0.20f, cy - rectH * 0.12f))
    drawCircle(color, bw * 0.38f, Offset(cx, cy - rectH * 0.35f))
    drawCircle(color, bw * 0.32f, Offset(cx - bw * 0.40f, cy - rectH * 0.20f))
    drawCircle(color, bw * 0.32f, Offset(cx + bw * 0.45f, cy - rectH * 0.22f))
}
