package com.example.skycast.data.model

/**
 * 城市（含下属区县/地点列表）
 */
data class City(
    val name: String,
    val districts: List<String> = emptyList(),
)

/**
 * 省份（含下属城市列表）
 */
data class Province(
    val province: String,
    val cities: List<City> = emptyList(),
)
