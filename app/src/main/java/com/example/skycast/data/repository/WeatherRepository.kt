package com.example.skycast.data.repository

import com.example.skycast.BuildConfig
import com.example.skycast.data.local.CachedWeatherDao
import com.example.skycast.data.local.CachedWeatherEntity
import com.example.skycast.data.model.CombinedWeather
import com.example.skycast.data.model.ForecastDay
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.WeatherApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val cacheDao: CachedWeatherDao,
) {

    private val id: String get() = BuildConfig.WEATHER_API_ID
    private val key: String get() = BuildConfig.WEATHER_API_KEY
    private val gson = Gson()

    suspend fun getCombinedWeather(
        province: String,
        city: String,
        district: String? = null,
        displayPlace: String = "$province $city",
    ): Result<CombinedWeather> = coroutineScope {
        try {
            val precisePlace = district ?: city

            val forecastDeferred = async {
                fetchForecast(province, precisePlace)
            }
            val realtimeDeferred = async {
                fetchRealtimeWithFallback(province, city, district)
            }

            val forecast = forecastDeferred.await()
            val (realtime, realtimePlace) = realtimeDeferred.await()

            if (forecast == null && realtime == null) {
                // 网络失败，尝试从缓存恢复
                val cached = cacheDao.get()
                if (cached != null) {
                    val combined = gson.fromJson(cached.combinedJson, CombinedWeather::class.java)
                    Result.success(combined.copy(isFromCache = true))
                } else {
                    Result.failure(Exception("天气数据获取失败，请检查网络或更换地点"))
                }
            } else {
                val combined = CombinedWeather(
                    realtime = realtime,
                    forecast = forecast,
                    displayPlace = displayPlace,
                    realtimePlace = realtimePlace ?: displayPlace,
                    isFromCache = false,
                )
                // 异步写缓存，不阻塞返回
                withContext(Dispatchers.IO) {
                    try {
                        cacheDao.upsert(
                            CachedWeatherEntity(
                                id = 1,
                                combinedJson = gson.toJson(combined),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    } catch (_: Exception) { /* 缓存写失败不影响主流程 */ }
                }
                Result.success(combined)
            }
        } catch (e: Exception) {
            // 任何异常都尝试读缓存
            val cached = cacheDao.get()
            if (cached != null) {
                val combined = gson.fromJson(cached.combinedJson, CombinedWeather::class.java)
                Result.success(combined.copy(isFromCache = true))
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchForecast(province: String, place: String): List<ForecastDay>? {
        return try {
            val resp = apiService.getWeather15Day(id, key, province, place)
            if (resp.code == 200 && !resp.data.isNullOrEmpty()) resp.data else null
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchRealtimeWithFallback(
        province: String,
        city: String,
        district: String?,
    ): Pair<WeatherResponse?, String?> {
        val precisePlace = district ?: city
        fetchRealtime(province, precisePlace)?.let { return it to precisePlace }

        if (district != null && district != city) {
            fetchRealtime(province, city)?.let { return it to city }
        }

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
