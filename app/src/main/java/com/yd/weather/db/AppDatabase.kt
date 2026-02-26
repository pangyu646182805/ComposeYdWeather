package com.yd.weather.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yd.weather.db.model.CityData

@Database(
    entities = [
        CityData::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "yd_weather_database"
    }

    abstract fun weatherDao(): WeatherDao
}