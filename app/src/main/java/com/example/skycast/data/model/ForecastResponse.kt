package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 新 API tqybmoji15.php 返回结构
 * 15日预报，不含实时数据（nowinfo/预警/日出日落）
 */
data class ForecastResponse(
    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("msg")
    val msg: String = "",

    @SerializedName("place")
    val place: String = "",

    @SerializedName("data")
    val data: List<ForecastDay>? = null,
)
