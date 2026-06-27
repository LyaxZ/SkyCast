package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * apihz.cn 天气 API 完整返回（扁平 JSON 结构）
 */
data class WeatherResponse(
    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("msg")
    val msg: String = "",

    @SerializedName("guo")
    val guo: String = "",

    @SerializedName("sheng")
    val sheng: String = "",

    @SerializedName("shi")
    val shi: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("weather1")
    val weather1: String = "",

    @SerializedName("weather2")
    val weather2: String = "",

    @SerializedName("wd1")
    val wd1: String = "",

    @SerializedName("wd2")
    val wd2: String = "",

    @SerializedName("winddirection1")
    val windDirection1: String = "",

    @SerializedName("winddirection2")
    val windDirection2: String = "",

    @SerializedName("windleve1")
    val windLevel1: String = "",

    @SerializedName("windleve2")
    val windLevel2: String = "",

    @SerializedName("weather1img")
    val weather1Img: String = "",

    @SerializedName("weather2img")
    val weather2Img: String = "",

    @SerializedName("lon")
    val lon: String = "",

    @SerializedName("lat")
    val lat: String = "",

    @SerializedName("uptime")
    val uptime: String = "",

    // ---- 嵌套对象 ----

    @SerializedName("nowinfo")
    val nowInfo: NowInfo? = null,

    @SerializedName("alarm")
    val alarm: List<AlarmInfo>? = null,

    @SerializedName("suntimes")
    val sunTimes: List<SunTime>? = null,

    @SerializedName("hour1")
    val hour1: List<HourWeather>? = null,

    // 第2~7天
    @SerializedName("weatherday2")
    val weatherDay2: DayWeather? = null,

    @SerializedName("weatherday3")
    val weatherDay3: DayWeather? = null,

    @SerializedName("weatherday4")
    val weatherDay4: DayWeather? = null,

    @SerializedName("weatherday5")
    val weatherDay5: DayWeather? = null,

    @SerializedName("weatherday6")
    val weatherDay6: DayWeather? = null,

    @SerializedName("weatherday7")
    val weatherDay7: DayWeather? = null,
)
