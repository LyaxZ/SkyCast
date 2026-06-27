package com.example.skycast.data.location

/**
 * 定位 + 逆地理编码结果
 * province / city 已去掉"省""市""区"等后缀，可直接传给天气 API
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,

    /** 去后缀后的省份名，如 "北京"、"四川" */
    val province: String,

    /** 去后缀后的城市名，如 "北京"、"成都" */
    val city: String,
)
