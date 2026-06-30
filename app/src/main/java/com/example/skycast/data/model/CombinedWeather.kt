package com.example.skycast.data.model

/**
 * 合并两个 API 结果的包装类
 *
 * @param realtime 旧 API（tqyb.php）返回的实时数据，包含 nowinfo/预警/日出日落
 * @param forecast 新 API（tqybmoji15.php）返回的 15 天预报列表
 * @param displayPlace 展示用地点名（精确到用户选择的层级）
 * @param realtimePlace 实时数据实际来源地点（fallback 后的上级地名）
 */
data class CombinedWeather(
    val realtime: WeatherResponse?,
    val forecast: List<ForecastDay>?,
    val displayPlace: String,
    val realtimePlace: String,
    val isFromCache: Boolean = false,
)
