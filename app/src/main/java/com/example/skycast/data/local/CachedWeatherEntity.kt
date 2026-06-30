package com.example.skycast.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val id: Int = 1,
    val combinedJson: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
