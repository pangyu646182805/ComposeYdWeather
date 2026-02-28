package com.yd.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherBgModel(
    val supportEdit: Boolean = false,
    val isSelected: Boolean = false,
    val colors: List<ULong>,
    val nightColors: List<ULong>,
)
