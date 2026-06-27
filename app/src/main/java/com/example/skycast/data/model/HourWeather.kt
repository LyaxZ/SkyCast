package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 各时段天气数据
 */
data class HourWeather(
    @SerializedName("时间")
    val time: String = "",

    @SerializedName("天气")
    val weather: String = "",

    @SerializedName("图标")
    val icon: String = "",

    @SerializedName("气温")
    val temperature: String = "",

    @SerializedName("降水")
    val precipitation: String = "",

    @SerializedName("风速")
    val windSpeed: String = "",

    @SerializedName("风向")
    val windDirection: String = "",

    @SerializedName("气压")
    val pressure: String = "",

    @SerializedName("湿度")
    val humidity: String = "",

    @SerializedName("云量")
    val cloudCover: String = "",
)
