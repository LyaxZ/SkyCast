package com.example.skycast.data.repository

import android.content.Context
import com.example.skycast.data.model.Province
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cache: List<Province>? = null

    /** 从 assets/china_cities.json 加载行政区划数据，IO 线程执行，首次后内存缓存 */
    suspend fun loadProvinces(): List<Province> {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val json = context.assets.open("china_cities.json")
                    .bufferedReader()
                    .use { it.readText() }
                val type = object : TypeToken<List<Province>>() {}.type
                val result: List<Province> = Gson().fromJson(json, type)
                cache = result
                result
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
