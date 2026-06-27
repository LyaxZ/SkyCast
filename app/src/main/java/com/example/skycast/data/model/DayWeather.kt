package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 7天天气预报中每一天的数据
 */
data class DayWeather(
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
)
