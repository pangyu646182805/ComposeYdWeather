package com.yd.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class SunriseAndSunset(
    val sunrise: String? = null,
    val sunset: String? = null,
)
