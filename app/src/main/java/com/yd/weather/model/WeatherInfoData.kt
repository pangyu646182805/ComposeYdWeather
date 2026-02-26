package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherInfoData(
    val bgPic: String? = null,
    val smPic: String? = null,
    val wthr: String? = null,
    val wd: String? = null,
    val wp: String? = null,
    @SerialName("third_type")
    val thirdType: String? = null,
    val type: Int = 0,
)
