package com.yd.weather.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "city_data")
@Serializable
data class CityData(
    @PrimaryKey
    val key: String = "",

    @ColumnInfo(name = "city_level_name")
    @SerialName("city_level_name")
    val cityLevelName: String? = null,

    val name: String? = null,

    val street: String? = null,

    val country: String? = null,

    val upper: String? = null,

    val prov: String? = null,

    @ColumnInfo(name = "cityid")
    @SerialName("cityid")
    val cityId: String? = null,

    @ColumnInfo(name = "city_level_id")
    @SerialName("city_level_id")
    val cityLevelId: String? = null,

    @ColumnInfo(defaultValue = "0")
    val isLocationCity: Boolean = false,

    @Embedded(prefix = "weather_data_")
    val weatherData: SimpleWeatherData? = null,
)
