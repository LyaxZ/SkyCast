package com.example.skycast.data.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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

/**
 * 从 assets/china_cities.json 加载行政区划数据并缓存在内存
 */
@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val provinces: List<Province> by lazy { loadFromAssets() }

    private fun loadFromAssets(): List<Province> {
        return try {
            val json = context.assets.open("china_cities.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Province>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}
