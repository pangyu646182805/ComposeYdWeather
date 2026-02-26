package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherObserveData(
    val day: WeatherInfoData? = null,
    val night: WeatherInfoData? = null,
    val type: Int = 0,
    @SerialName("third_type")
    val thirdType: String? = null,
    @SerialName("tigan")
    val tiGan: String? = null,
    val wthr: String? = null,
    val wd: String? = null,
    val wp: String? = null,
    @SerialName("shidu")
    val shiDu: String? = null,
    @SerialName("uv_level")
    val uvLevel: String? = null,
    val pressure: String? = null,
    val visibility: String? = null,
    val temp: Int = 0,
    @SerialName("uv_index")
    val uvIndex: Int = 0,
    @SerialName("uv_index_max")
    val uvIndexMax: Int = 0,
)
