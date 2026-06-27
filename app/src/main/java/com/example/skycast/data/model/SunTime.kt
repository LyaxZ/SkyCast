package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 日出日落时间表
 */
data class SunTime(
    @SerializedName("date")
    val date: String = "",

    @SerializedName("date_formatted")
    val dateFormatted: String = "",

    @SerializedName("weekday_cn")
    val weekdayCn: String = "",

    @SerializedName("sunrise")
    val sunrise: String = "",

    @SerializedName("sunset")
    val sunset: String = "",

    @SerializedName("civil_twilight_begin")
    val civilTwilightBegin: String = "",

    @SerializedName("civil_twilight_end")
    val civilTwilightEnd: String = "",

    @SerializedName("day_length")
    val dayLength: String = "",

    @SerializedName("night_length")
    val nightLength: String = "",
)
