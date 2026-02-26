package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherHourData(
    val time: String? = null,
    @SerialName("shidu")
    val shiDu: String? = null,
    val type: Int = 0,
    @SerialName("third_type")
    val thirdType: String? = null,
    val wd: String? = null,
    val wp: String? = null,
    val wthr: Int = 0,
    val sunriseAndSunset: SunriseAndSunset? = null,
)
