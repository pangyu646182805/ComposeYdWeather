package com.yd.weather.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object WeatherBgRoutes {
    @Serializable
    data object WeatherBgList : NavKey

    @Serializable
    data class WeatherBgEdit(
        val weatherType: String,
        val colorsJson: String = "",
        val nightColorsJson: String = "",
        val isEdit: Boolean = false,
        val isPreviewMode: Boolean = false
    ) : NavKey
}
