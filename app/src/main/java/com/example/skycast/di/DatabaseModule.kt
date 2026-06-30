package com.example.skycast.di

import android.content.Context
import androidx.room.Room
import com.example.skycast.data.local.AppDatabase
import com.example.skycast.data.local.CachedWeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "skycast_cache.db",
        ).build()
    }

    @Provides
    @Singleton
    fun provideCachedWeatherDao(db: AppDatabase): CachedWeatherDao {
        return db.cachedWeatherDao()
    }
}
