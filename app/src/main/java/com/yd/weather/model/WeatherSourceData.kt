package com.yd.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherSourceData(
    val title: String? = null,
)
