package com.example.skycast.data.repository

import com.example.skycast.BuildConfig
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
) {

    /**
     * 获取指定城市的天气预报，返回 Flow<Result<WeatherResponse>>
     * API 凭证从 BuildConfig 自动读取
     */
    fun getWeather(
        sheng: String = "",
        place: String,
        day: Int = 1,
    ): Flow<Result<WeatherResponse>> = flow {
        try {
            val response = apiService.getWeatherNow(
                id = BuildConfig.WEATHER_API_ID,
                key = BuildConfig.WEATHER_API_KEY,
                sheng = sheng,
                place = place,
                day = day,
            )
            if (response.code == 200) {
                emit(Result.success(response))
            } else {
                emit(Result.failure(Exception(response.msg.ifEmpty { "请求失败，code=${response.code}" })))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
