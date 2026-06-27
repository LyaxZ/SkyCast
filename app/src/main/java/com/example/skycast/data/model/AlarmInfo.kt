package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 天气预警信息
 */
data class AlarmInfo(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("signaltype")
    val signalType: String = "",

    @SerializedName("signallevel")
    val signalLevel: String = "",

    @SerializedName("effective")
    val effective: String = "",

    @SerializedName("eventType")
    val eventType: String = "",

    @SerializedName("severity")
    val severity: String = "",

    @SerializedName("type")
    val type: String = "",
)
