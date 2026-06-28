package com.example.skycast.data.repository

import com.example.skycast.BuildConfig
import com.example.skycast.data.model.CombinedWeather
import com.example.skycast.data.model.ForecastDay
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.WeatherApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
) {

    private val id: String get() = BuildConfig.WEATHER_API_ID
    private val key: String get() = BuildConfig.WEATHER_API_KEY

    /**
     * 合并两个 API 的结果
     *
     * 新 API（tqybmoji15.php）：直接用精确地点，支持区县/景点 → 拿 15 天预报
     * 旧 API（tqyb.php）：先试精确地点，失败则逐级向上（城市 → 省份）→ 拿实时数据
     *
     * @param province 省份名（已去后缀）
     * @param city     城市名
     * @param district 区县/具体地点名（可选，null 则 city 为精确地点）
     * @param displayPlace 展示用完整地名
     */
    suspend fun getCombinedWeather(
        province: String,
        city: String,
        district: String? = null,
        displayPlace: String = "$province $city",
    ): Result<CombinedWeather> = coroutineScope {
        try {
            val precisePlace = district ?: city

            // 并行请求两个 API
            val forecastDeferred = async {
                fetchForecast(province, precisePlace)
            }
            val realtimeDeferred = async {
                fetchRealtimeWithFallback(province, city, district)
            }

            val forecast = forecastDeferred.await()
            val (realtime, realtimePlace) = realtimeDeferred.await()

            if (forecast == null && realtime == null) {
                Result.failure(Exception("天气数据获取失败，请检查网络或更换地点"))
            } else {
                Result.success(
                    CombinedWeather(
                        realtime = realtime,
                        forecast = forecast,
                        displayPlace = displayPlace,
                        realtimePlace = realtimePlace ?: displayPlace,
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 新 API：直接请求精确地点（支持区县及以下） */
    private suspend fun fetchForecast(province: String, place: String): List<ForecastDay>? {
        return try {
            val resp = apiService.getWeather15Day(id, key, province, place)
            if (resp.code == 200 && !resp.data.isNullOrEmpty()) resp.data else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 旧 API fallback 链：精确地点 → 城市 → 省份
     * 返回 Pair<WeatherResponse?, realtimePlace>
     */
    private suspend fun fetchRealtimeWithFallback(
        province: String,
        city: String,
        district: String?,
    ): Pair<WeatherResponse?, String?> {
        // ① 先试精确地点（district 或 city）
        val precisePlace = district ?: city
        fetchRealtime(province, precisePlace)?.let { return it to precisePlace }

        // ② 如果 district 不是 city 本身，fallback 到 city
        if (district != null && district != city) {
            fetchRealtime(province, city)?.let { return it to city }
        }

        // ③ 最后 fallback 到省份级别
        fetchRealtime(province, province)?.let { return it to province }

        return null to null
    }

    private suspend fun fetchRealtime(sheng: String, place: String): WeatherResponse? {
        return try {
            val resp = apiService.getWeatherNow(id, key, sheng, place)
            if (resp.code == 200) resp else null
        } catch (_: Exception) {
            null
        }
    }
}
