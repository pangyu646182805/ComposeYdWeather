package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherEnvData(
    @SerialName("aqi_level_name")
    val aqiLevelName: String? = null,
    val mp: String? = null,
    val quality: String? = null,
    val aqi: Int = 0,
    @SerialName("aqi_level")
    val aqiLevel: Int = 0,
    val co: Int = 0,
    val no2: Int = 0,
    val o3: Int = 0,
    val pm10: Int = 0,
    val pm25: Int = 0,
    val so2: Int = 0,
)
