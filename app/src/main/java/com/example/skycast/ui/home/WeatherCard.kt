package com.example.skycast.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.data.model.AlarmInfo
import com.example.skycast.data.model.SunTime
import com.example.skycast.data.model.WeatherResponse

// ─── 24h 时间 + 天气差异化渐变 ─────────────────────

data class CardGradient(val top: Color, val bottom: Color, val tint: Color)

/**
 * 渐变策略分四类：
 * 1. 晴：纯 24h 时间渐变
 * 2. 阴/雨/雷/雾：不跟时间，纯灰度（白天浅灰/夜晚深灰）
 * 3. 多云：时间渐变 + 白天叠加更多灰，夜晚叠加蓝
 * 4. 雪：时间渐变 + 夜晚叠加蓝
 */
fun cardGradient(hour: Int, weatherText: String = "晴"): CardGradient {
    val t = weatherText.trim()
    val cat = weatherCategory(t)

    // ── 雾/阴/雨/雷：纯灰度，逐个更暗 ──
    if (cat in setOf(WeatherCat.FOG, WeatherCat.OVERCAST, WeatherCat.RAIN, WeatherCat.THUNDER)) {
        val isDay = hour in 7..16
        val isTwilight = hour in 5..6 || hour in 17..19
        return when (cat) {
            WeatherCat.FOG -> when {
                isDay -> CardGradient(Color(0xFF8E8EB2), Color(0xFF9E9EC5), Color.White)
                isTwilight -> CardGradient(Color(0xFF72729A), Color(0xFF8484AE), Color.White)
                else -> CardGradient(Color(0xFF2E2E52), Color(0xFF3E3E64), Color.White)
            }
            WeatherCat.OVERCAST -> when {
                isDay -> CardGradient(Color(0xFF76769E), Color(0xFF8888B0), Color.White)
                isTwilight -> CardGradient(Color(0xFF5E5E88), Color(0xFF6E6E9A), Color.White)
                else -> CardGradient(Color(0xFF24244A), Color(0xFF34345E), Color.White)
            }
            WeatherCat.RAIN -> when {
                isDay -> CardGradient(Color(0xFF5E5E8C), Color(0xFF6E6EA0), Color.White)
                isTwilight -> CardGradient(Color(0xFF48487C), Color(0xFF5A5A8C), Color.White)
                else -> CardGradient(Color(0xFF1A1A40), Color(0xFF2A2A54), Color.White)
            }
            WeatherCat.THUNDER -> when {
                isDay -> CardGradient(Color(0xFF444472), Color(0xFF545486), Color.White)
                isTwilight -> CardGradient(Color(0xFF323260), Color(0xFF444474), Color.White)
                else -> CardGradient(Color(0xFF101036), Color(0xFF20204A), Color.White)
            }
            else -> CardGradient(Color(0xFF8A8A9A), Color(0xFFA0A0B0), Color.White) // unreachable
        }
    }

    // ── 其余：时间渐变 + 叠加 ──
    val base = timeGradient(hour)
    val isNight = hour in 20..23 || hour in 0..4

    return when (cat) {
        WeatherCat.SUNNY -> base
        WeatherCat.CLOUDY -> {
            if (isNight) {
                // 夜晚：时间渐变 + 蓝调
                CardGradient(
                    top = lerp(base.top, Color(0xFF2A3A60), 0.35f),
                    bottom = lerp(base.bottom, Color(0xFF3A4A70), 0.35f),
                    tint = lerp(base.tint, Color(0xFFC0D0F0), 0.3f),
                )
            } else {
                // 白天/清晨/傍晚：时间渐变 + 更多灰
                CardGradient(
                    top = lerp(base.top, Color(0xFF8A8A98), 0.38f),
                    bottom = lerp(base.bottom, Color(0xFFA0A0AE), 0.38f),
                    tint = lerp(base.tint, Color(0xFFF0F0F0), 0.30f),
                )
            }
        }
        WeatherCat.SNOW -> {
            if (isNight) {
                // 夜晚：时间渐变 + 蓝调
                CardGradient(
                    top = lerp(base.top, Color(0xFF3A4A6E), 0.25f),
                    bottom = lerp(base.bottom, Color(0xFF4A5A7E), 0.25f),
                    tint = lerp(base.tint, Color(0xFFD0D8F8), 0.20f),
                )
            } else {
                // 白天保持时间渐变 + 轻微雪白
                CardGradient(
                    top = lerp(base.top, Color(0xFFD0D8E8), 0.20f),
                    bottom = lerp(base.bottom, Color(0xFFE0E8F0), 0.20f),
                    tint = base.tint,
                )
            }
        }
        else -> base
    }
}

private enum class WeatherCat { SUNNY, CLOUDY, FOG, OVERCAST, RAIN, THUNDER, SNOW }

private fun weatherCategory(weatherText: String): WeatherCat {
    val t = weatherText.trim()
    return when {
        t.contains("雷") || t.contains("暴") -> WeatherCat.THUNDER
        t.contains("雨") || t.contains("阵") -> WeatherCat.RAIN
        t.contains("雾") || t.contains("霾") || t.contains("沙") || t.contains("尘") -> WeatherCat.FOG
        t == "阴" -> WeatherCat.OVERCAST
        t.contains("雪") -> WeatherCat.SNOW
        t == "少云" || t == "多云" -> WeatherCat.CLOUDY
        else -> WeatherCat.SUNNY
    }
}

/** 晴天的 24h 纯时间渐变 */
private fun timeGradient(hour: Int): CardGradient = when (hour) {
    // 入夜→0h：越来越暗
    20 -> CardGradient(Color(0xFF1A2A4A), Color(0xFF2D3D6E), Color(0xFFD0D8E8))
    21 -> CardGradient(Color(0xFF152540), Color(0xFF253560), Color(0xFFCCD4E4))
    22 -> CardGradient(Color(0xFF102038), Color(0xFF203058), Color(0xFFC8D2E0))
    23 -> CardGradient(Color(0xFF0C1A2E), Color(0xFF1A2848), Color(0xFFC4CED8))
    // 0h 最暗 → 4h 逐渐变亮（走向清晨）
    0  -> CardGradient(Color(0xFF0A1628), Color(0xFF162440), Color(0xFFC0CAD4))
    1  -> CardGradient(Color(0xFF0C1A2C), Color(0xFF182844), Color(0xFFC2CCD6))
    2  -> CardGradient(Color(0xFF0E1C30), Color(0xFF1C2C48), Color(0xFFC4CED8))
    3  -> CardGradient(Color(0xFF122034), Color(0xFF20304C), Color(0xFFC8D2DC))
    4  -> CardGradient(Color(0xFF162638), Color(0xFF243450), Color(0xFFCAD4DE))
    // 清晨/日出：蓝→橙
    5  -> CardGradient(Color(0xFF2C3E6B), Color(0xFFE8956A), Color(0xFFFFF0E0))
    6  -> CardGradient(Color(0xFF4A7AB5), Color(0xFFF0A860), Color(0xFFFFF5E8))
    // 白天：天蓝渐变
    7  -> CardGradient(Color(0xFF5B8CC0), Color(0xFF87CEEB), Color.White)
    8  -> CardGradient(Color(0xFF4A90D9), Color(0xFF6DB3E8), Color.White)
    9  -> CardGradient(Color(0xFF5098E0), Color(0xFF78C0F0), Color.White)
    10 -> CardGradient(Color(0xFF55A0E8), Color(0xFF80C8F5), Color.White)
    11 -> CardGradient(Color(0xFF58A8F0), Color(0xFF88D0F8), Color.White)
    12 -> CardGradient(Color(0xFF5AAAF2), Color(0xFF8CD4FA), Color.White)
    13 -> CardGradient(Color(0xFF58A8F0), Color(0xFF88D0F8), Color.White)
    14 -> CardGradient(Color(0xFF55A0E8), Color(0xFF80C8F5), Color.White)
    15 -> CardGradient(Color(0xFF4A90D9), Color(0xFF6DB3E8), Color.White)
    16 -> CardGradient(Color(0xFF4488D0), Color(0xFF60A8E0), Color.White)
    // 傍晚/日落：蓝→橙
    17 -> CardGradient(Color(0xFF5578B8), Color(0xFFF0A860), Color(0xFFFFF5E8))
    18 -> CardGradient(Color(0xFF4A60A0), Color(0xFFE8956A), Color(0xFFFFF0E0))
    19 -> CardGradient(Color(0xFF3A5088), Color(0xFFD08060), Color(0xFFFFE8D0))
    else -> CardGradient(Color(0xFF55A0E8), Color(0xFF80C8F5), Color.White)
}

// ─── 卡片组件 ──────────────────────────────────────

@Composable
fun WeatherCard(
    weather: WeatherResponse,
    displayPlace: String,
    realtimePlace: String,
    modifier: Modifier = Modifier,
    gradientOverride: CardGradient? = null,
) {
    val weatherText = weather.weather1.ifEmpty { "晴" }
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val gradient = gradientOverride ?: cardGradient(hour, weatherText)
    val type = classify(weatherText)
    val isNight = hour < 6 || hour >= 20

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(gradient.top, gradient.bottom)))
                },
        ) {
            WeatherParticles(weatherType = type, isNight = isNight, modifier = Modifier.matchParentSize())

            // 主布局：右上角地址 + 大图标居中靠上 + 温度/详情在底部
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                // 右上角地址
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = realtimePlace,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = gradient.tint.copy(alpha = 0.8f),
                    )
                }

                Spacer(Modifier.height(4.dp))

                // 天气图标（大号，居中）
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    WeatherIcon(
                        weatherText = weatherText,
                        modifier = Modifier.size(88.dp),
                        tint = gradient.tint,
                    )
                }

                Spacer(Modifier.height(2.dp))

                // 温度：数字居中 + ° 小号上标
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        weather.wd1,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = gradient.tint,
                    )
                    Text(
                        "°",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = gradient.tint,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // 天气描述
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "${weather.weather1} 转 ${weather.weather2}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = gradient.tint.copy(alpha = 0.9f),
                    )
                }

                // 体感温度
                weather.nowInfo?.let { nowInfo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "体感 ${nowInfo.feelst}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = gradient.tint.copy(alpha = 0.7f),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 底部信息：湿度 / 风向 / 气压
                weather.nowInfo?.let { nowInfo ->
                    DetailGrid(
                        nowInfo.humidity,
                        nowInfo.windDirection,
                        nowInfo.windScale,
                        nowInfo.pressure,
                        gradient.tint,
                    )
                }

                // 日出日落
                weather.sunTimes?.firstOrNull()?.let { sun ->
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        SunRow(sun, gradient.tint)
                    }
                }

                // 预警
                if (!weather.alarm.isNullOrEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    weather.alarm.forEach { AlarmBadge(it) }
                }

                // 更新时间
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (realtimePlace != displayPlace)
                            "数据来源：$realtimePlace · 更新 ${weather.uptime}"
                        else
                            "更新于 ${weather.uptime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.tint.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailGrid(humidity: Double, windDir: String, windScale: String, pressure: Double, tint: Color) {
    val ha = tint.copy(alpha = 0.8f)
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text("💧 湿度", style = MaterialTheme.typography.labelSmall, color = ha)
            Text("${humidity}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = tint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("🌬 $windDir", style = MaterialTheme.typography.labelSmall, color = ha)
            Text(windScale, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = tint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("📊 气压", style = MaterialTheme.typography.labelSmall, color = ha)
            Text("${pressure} hPa", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = tint)
        }
    }
}

@Composable
private fun SunRow(sun: SunTime, tint: Color) {
    Text("🌅 日出 ${sun.sunrise}   🌇 日落 ${sun.sunset}",
        style = MaterialTheme.typography.bodySmall, color = tint.copy(alpha = 0.7f))
}

@Composable
private fun AlarmBadge(alarm: AlarmInfo) {
    Text("⚠ ${alarm.title}（${alarm.signalLevel}）",
        style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
}
