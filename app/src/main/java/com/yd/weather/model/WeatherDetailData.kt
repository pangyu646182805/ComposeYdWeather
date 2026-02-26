package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDetailData(
    val date: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val type: Int = 0,
    @SerialName("third_type")
    val thirdType: String? = null,
    val high: Int = 0,
    val low: Int = 0,
    val wthr: String? = null,
    val wd: String? = null,
    val wp: String? = null,
    @SerialName("aqi_level_name")
    val aqiLevelName: String? = null,
    val aqi: Int = 0,
    @SerialName("uv_index")
    val uvIndex: Int = 0,
    @SerialName("uv_index_max")
    val uvIndexMax: Int = 0,
    @SerialName("uv_level")
    val uvLevel: String? = null,
    val visibility: String? = null,
    val day: WeatherInfoData? = null,
    val night: WeatherInfoData? = null,
)
