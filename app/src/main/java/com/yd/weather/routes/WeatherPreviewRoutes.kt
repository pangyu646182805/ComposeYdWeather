package com.yd.weather.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object WeatherPreviewRoutes {
    @Serializable
    data class WeatherPreview(val cityId: String? = null) : NavKey
}