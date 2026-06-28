package com.example.skycast.data.remote

import com.example.skycast.data.model.ForecastResponse
import com.example.skycast.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * 查询实时天气（旧 API tqyb.php）
     * 返回 nowinfo / alarm / suntimes / hour 等实时数据
     * 仅支持到市级，区县及以下可能返回失败
     */
    @GET("api/tianqi/tqyb.php")
    suspend fun getWeatherNow(
        @Query("id") id: String,
        @Query("key") key: String,
        @Query("sheng") sheng: String = "",
        @Query("place") place: String,
        @Query("day") day: Int = 1,
    ): WeatherResponse

    /**
     * 查询 15 日天气预报（新 API tqybmoji15.php）
     * 支持到区县及具体地点（景点等）
     * 不含实时 nowinfo / 预警 / 日出日落
     */
    @GET("api/tianqi/tqybmoji15.php")
    suspend fun getWeather15Day(
        @Query("id") id: String,
        @Query("key") key: String,
        @Query("sheng") sheng: String,
        @Query("place") place: String,
    ): ForecastResponse
}
