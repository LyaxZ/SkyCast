package com.example.skycast.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.data.model.CityRepository
import com.example.skycast.data.model.CombinedWeather
import com.example.skycast.data.model.ForecastDay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cityRepository: CityRepository,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPicker by remember { mutableStateOf(value = false) }

    // 权限拒绝 / 定位失败后首次进入时自动弹出选择器
    if (uiState.needManualInput && !showPicker) {
        val provinces = cityRepository.provinces
        if (provinces.isNotEmpty()) {
            showPicker = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "SkyCast ☁️",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ---- 加载中 ----
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
            return@Column
        }

        // ---- 错误提示 ----
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        val combined = uiState.combinedWeather

        // ---- 位置标签（可点击弹出选择器） ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (combined != null) "📍 ${combined.displayPlace}" else "📍 点击选择城市",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "▼",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---- 刷新定位按钮 ----
        Button(
            onClick = { viewModel.loadWeatherByLocation() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("刷新定位")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---- 天气数据展示 ----
        if (combined != null) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 实时天气卡片
                if (combined.realtime != null) {
                    item { RealtimeCard(combined) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // 15日预报标题
                if (!combined.forecast.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "📅 15日预报",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        )
                    }
                }

                // 预报列表
                itemsIndexed(combined.forecast ?: emptyList()) { index, day ->
                    ForecastRow(day)
                    val lastIndex = (combined.forecast?.size ?: 1) - 1
                if (index < lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }

    // ---- 城市选择器弹窗 ----
    if (showPicker) {
        CityPickerDialog(
            provinces = cityRepository.provinces,
            onDismiss = { showPicker = false },
            onSelected = { province, city, district ->
                showPicker = false
                viewModel.selectCity(province, city, district)
            },
        )
    }
}

@Composable
private fun RealtimeCard(combined: CombinedWeather) {
    val weather = combined.realtime ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 来源标注
            Text(
                text = if (combined.realtimePlace != combined.displayPlace)
                    "实时数据来源：${combined.realtimePlace}"
                else
                    "实时天气",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 当天天气概况
            Text(
                text = "${weather.weather1}  ${weather.wd1}°",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "夜间 ${weather.weather2}  ${weather.wd2}°",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 实时详情
            val now = weather.nowInfo
            if (now != null) {
                WeatherDetailRow("实时温度", "${now.temperature}°C")
                WeatherDetailRow("体感温度", "${now.feelst}°C")
                WeatherDetailRow("湿度", "${now.humidity}%")
                WeatherDetailRow("气压", "${now.pressure} hPa")
                WeatherDetailRow("风向", now.windDirection)
                WeatherDetailRow("风速", "${now.windSpeed} m/s")
                WeatherDetailRow("风力等级", now.windScale)
                WeatherDetailRow("降水量", "${now.precipitation} mm")
            }

            // 风力
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "白天 ${weather.windDirection1} ${weather.windLevel1}  |  夜间 ${weather.windDirection2} ${weather.windLevel2}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // 日出日落
            val sun = weather.sunTimes?.firstOrNull()
            if (sun != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "日出 ${sun.sunrise}  日落 ${sun.sunset}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // 预警
            val alarms = weather.alarm
            if (!alarms.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                alarms.forEach { alarm ->
                    Text(
                        text = "⚠ ${alarm.title}（${alarm.signalLevel}）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // 更新时间
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "更新时间：${weather.uptime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ForecastRow(day: ForecastDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 星期 + 日期
        Column(modifier = Modifier.width(56.dp)) {
            Text(
                text = day.week1,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = day.week2,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 天气文字
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "白天 ${day.wea1}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "夜间 ${day.wea2}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 温度
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = day.wendu1,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = day.wendu2,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}
