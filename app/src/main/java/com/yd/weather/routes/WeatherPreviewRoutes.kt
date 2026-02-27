package com.yd.weather.routes

import kotlinx.serialization.Serializable

object WeatherPreviewRoutes {
    @Serializable
    data class WeatherPreview(val cityId: String? = null)
}