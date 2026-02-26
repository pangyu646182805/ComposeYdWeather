package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherMetaData(
    @SerialName("citykey")
    val cityKey: String? = null,
    val city: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    val upper: String? = null,
)
