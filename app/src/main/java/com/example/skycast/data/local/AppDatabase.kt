package com.example.skycast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedWeatherEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedWeatherDao(): CachedWeatherDao
}
