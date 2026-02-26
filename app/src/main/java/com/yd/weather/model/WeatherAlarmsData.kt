package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlarmsData(
    @SerialName("short_title")
    val shortTitle: String? = null,
    val desc: String? = null,
    @SerialName("pub_time")
    val pubTime: String? = null,
)
