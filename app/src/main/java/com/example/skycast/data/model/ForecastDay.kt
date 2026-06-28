package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

/**
 * 单日天气预报（新 API tqybmoji15.php 的 data[] 元素）
 */
data class ForecastDay(
    @SerializedName("week1")
    val week1: String = "",   // "周六"

    @SerializedName("week2")
    val week2: String = "",   // "06/27"

    @SerializedName("wea1")
    val wea1: String = "",    // "少云"（白天天气）

    @SerializedName("wea2")
    val wea2: String = "",    // "雷阵雨"（夜间天气）

    @SerializedName("wendu1")
    val wendu1: String = "",  // "25°"（最高温度）

    @SerializedName("wendu2")
    val wendu2: String = "",  // "17°"（最低温度）

    @SerializedName("img1")
    val img1: String = "",    // 白天天气图标 URL

    @SerializedName("img2")
    val img2: String = "",    // 夜间天气图标 URL
)
