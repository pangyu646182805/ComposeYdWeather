package com.yd.weather.model

import com.yd.weather.db.model.CityData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectCityData(
    @SerialName("hot_international")
    val hotInternational: List<CityData>? = null,
    @SerialName("hot_national")
    val hotNational: List<CityData>? = null,
)