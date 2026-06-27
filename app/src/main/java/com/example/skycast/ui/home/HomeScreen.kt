package com.example.skycast.ui.home

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }

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

        // ---- 手动输入模式 ----
        if (uiState.needManualInput) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("输入城市名，如 北京、成都") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (inputText.isNotBlank()) {
                            focusManager.clearFocus()
                            viewModel.searchByPlace(inputText.trim())
                        }
                    }
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        focusManager.clearFocus()
                        viewModel.searchByPlace(inputText.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("查询天气")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.loadWeatherByLocation() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("重新定位")
            }
        }

        // ---- 已有定位信息时显示重新定位按钮 ----
        if (!uiState.needManualInput && uiState.locationName.isNotEmpty()) {
            Text(
                text = "📍 ${uiState.locationName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.loadWeatherByLocation() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("刷新定位")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ---- 天气数据展示 ----
        val weather = uiState.weather
        if (weather != null) {
            WeatherContent(weather)
        }
    }
}

@Composable
private fun WeatherContent(weather: com.example.skycast.data.model.WeatherResponse) {
    val now = weather.nowInfo

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 当天天气
        Text(
            text = "${weather.weather1}  ${weather.wd1}°",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "夜间 ${weather.weather2}  ${weather.wd2}°",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 实时天气详情
        if (now != null) {
            WeatherDetailRow(label = "实时温度", value = "${now.temperature}°C")
            WeatherDetailRow(label = "体感温度", value = "${now.feelst}°C")
            WeatherDetailRow(label = "湿度", value = "${now.humidity}%")
            WeatherDetailRow(label = "气压", value = "${now.pressure} hPa")
            WeatherDetailRow(label = "风向", value = now.windDirection)
            WeatherDetailRow(label = "风速", value = "${now.windSpeed} m/s")
            WeatherDetailRow(label = "风力等级", value = now.windScale)
            WeatherDetailRow(label = "降水量", value = "${now.precipitation} mm")
        }

        // 风力
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "白天 ${weather.windDirection1} ${weather.windLevel1}  |  夜间 ${weather.windDirection2} ${weather.windLevel2}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // 更新时间
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "更新时间：${weather.uptime}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
