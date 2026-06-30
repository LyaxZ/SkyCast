package com.example.skycast.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DemoBg = Color(0xFF1A1A2E)

// 天气切换选项
private val weatherOptions = listOf("晴", "多云", "阴", "雨", "雷阵雨", "雪", "雾")

@Composable
fun PreviewScreen(
    onDismiss: () -> Unit,
) {
    var selectedWeather by remember { mutableStateOf("晴") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoBg)
            .padding(16.dp),
    ) {
        // 标题
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🎨 组件预览", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("✕ 关闭", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp,
                    modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(Modifier.height(20.dp))
        }

        // ─── 7 个天气图标 ───
        item {
            SectionTitle("☁️ 天气图标 (7种)")
            Spacer(Modifier.height(8.dp))

            val iconNames = listOf(
                "晴" to WeatherType.SUNNY,
                "多云" to WeatherType.CLOUDY,
                "阴" to WeatherType.OVERCAST,
                "雨" to WeatherType.RAINY,
                "雷阵雨" to WeatherType.THUNDERSTORM,
                "雪" to WeatherType.SNOWY,
                "雾" to WeatherType.FOGGY,
            )
            val rows = listOf(iconNames.take(4), iconNames.drop(4))
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { (name, _) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp),
                        ) {
                            Card(
                                modifier = Modifier.size(72.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A4A)),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    WeatherIcon(weatherText = name, size = 56.dp, tint = Color.White)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(name, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // ─── 24h 时间渐变 + 天气切换 ───
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle("🕐 24h 渐变配色")
            Spacer(Modifier.height(4.dp))

            // 天气切换按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                weatherOptions.forEach { w ->
                    val isSelected = w == selectedWeather
                    Text(
                        text = w,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF4A6FA5) else Color(0xFF2A2A4A))
                            .clickable { selectedWeather = w }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 每行6小时
            val hours = (0..23).toList()
            hours.chunked(6).forEach { hourRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    hourRow.forEach { h ->
                        val g = cardGradient(h, selectedWeather)
                        val label = when {
                            h == 5 -> "清晨"
                            h == 6 -> "日出"
                            h == 12 -> "正午"
                            h == 18 -> "日落"
                            h == 20 -> "入夜"
                            else -> "${h}h"
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(56.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp, 36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .drawBehind {
                                        drawRect(Brush.verticalGradient(listOf(g.top, g.bottom)))
                                    },
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ─── 粒子效果展示 ───
        item {
            Spacer(Modifier.height(8.dp))
            SectionTitle("✨ 粒子效果")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ParticleDemo("🌧 雨", WeatherType.RAINY)
                ParticleDemo("❄ 雪", WeatherType.SNOWY)
                ParticleDemo("☀ 阳光", WeatherType.SUNNY)
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ParticleDemo(label: String, type: WeatherType) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A4A)),
            contentAlignment = Alignment.Center,
        ) {
            WeatherParticles(weatherType = type, isNight = false, modifier = Modifier.fillMaxSize())
            if (type == WeatherType.SUNNY) {
                Text("☀️", fontSize = 28.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}
