package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 当前天气详细信息（nowinfo 嵌套对象）
 * 对应 apihz.cn 天气 API 返回的 nowinfo 字段
 */
data class NowInfo(
    @SerializedName("precipitation")
    val precipitation: Double = 0.0,

    @SerializedName("temperature")
    val temperature: Double = 0.0,

    @SerializedName("pressure")
    val pressure: Double = 0.0,

    @SerializedName("humidity")
    val humidity: Double = 0.0,

    @SerializedName("windDirection")
    val windDirection: String = "",

    @SerializedName("windDirectionDegree")
    val windDirectionDegree: Int = 0,

    @SerializedName("windSpeed")
    val windSpeed: Double = 0.0,

    @SerializedName("windScale")
    val windScale: String = "",

    @SerializedName("feelst")
    val feelst: Double = 0.0,

    @SerializedName("uptime")
    val uptime: String = "",
)
