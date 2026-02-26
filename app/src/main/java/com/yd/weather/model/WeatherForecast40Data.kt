package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecast40Data(
    @SerialName("average_temp")
    val averageTemp: Int = 0,
    @SerialName("up_days")
    val upDays: Int = 0,
    @SerialName("down_days")
    val downDays: Int = 0,
    @SerialName("rain_days")
    val rainDays: Int = 0,
)
