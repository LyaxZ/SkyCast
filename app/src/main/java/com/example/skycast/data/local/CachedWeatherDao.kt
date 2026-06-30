package com.example.skycast.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CachedWeatherDao {
    @Query("SELECT * FROM cached_weather WHERE id = 1")
    suspend fun get(): CachedWeatherEntity?

    @Upsert
    suspend fun upsert(entity: CachedWeatherEntity)
}
