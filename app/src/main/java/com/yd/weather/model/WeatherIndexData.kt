package com.yd.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherIndexData(
    val name: String? = null,
    val value: String? = null,
    val desc: String? = null,
    val ext: WeatherIndexExtData? = null,
)
