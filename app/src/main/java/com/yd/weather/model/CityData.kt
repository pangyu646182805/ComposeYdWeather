package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityData(
    @SerialName("cityLevelName")
    val cityLevelName: String? = null,
    val name: String? = null,
    val street: String? = null,
    val country: String? = null,
    val upper: String? = null,
    val prov: String? = null,
    val type: Int? = null,
    @SerialName("prov_en")
    val provEn: String? = null,
    @SerialName("cityid")
    val cityId: String? = null,
    @SerialName("city_level_id")
    val cityLevelId: String? = null,
)