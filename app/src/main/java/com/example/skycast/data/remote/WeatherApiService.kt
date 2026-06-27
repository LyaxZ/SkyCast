package com.example.skycast.data.remote

import com.example.skycast.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * 查询当前天气（apihz.cn 接口盒子）
     * GET http://81.68.85.14/api/tianqi/tqyb.php
     *
     * @param id     开发者 ID
     * @param key    开发者 KEY
     * @param sheng  省份名称（可选）
     * @param place  地点名称，如 "北京"、"广州"
     * @param day    查询天数 1~7，默认 1
     */
    @GET("api/tianqi/tqyb.php")
    suspend fun getWeatherNow(
        @Query("id") id: String,
        @Query("key") key: String,
        @Query("sheng") sheng: String = "",
        @Query("place") place: String,
        @Query("day") day: Int = 1,
    ): WeatherResponse
}
