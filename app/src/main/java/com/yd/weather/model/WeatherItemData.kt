package com.yd.weather.model

import com.yd.weather.config.Constants
import kotlinx.serialization.Serializable

@Serializable
data class WeatherItemData(
    val itemType: Int = Constants.ITEM_TYPE_WEATHER_HEADER,
    val weatherData: WeatherData? = null,
)
