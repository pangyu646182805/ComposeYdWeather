package com.yd.weather.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_data")
data class CityData(
    @PrimaryKey
    val id: Long,

    val key: String,

    @ColumnInfo(name = "city_level_name")
    val cityLevelName: String?,

    val name: String?,

    val street: String?,

    val country: String?,

    val upper: String?,

    val prov: String?,

    @ColumnInfo(name = "cityid")
    val cityId: String?,

    @ColumnInfo(name = "city_level_id")
    val cityLevelId: String?,

    @ColumnInfo(defaultValue = "0")
    val isLocationCity: Boolean = false,

    @Embedded(prefix = "weather_data_")
    val weatherData: SimpleWeatherData? = null,
)
