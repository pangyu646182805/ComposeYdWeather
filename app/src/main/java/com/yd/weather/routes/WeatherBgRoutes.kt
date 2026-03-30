package com.yd.weather.routes

import kotlinx.serialization.Serializable

object WeatherBgRoutes {
    @Serializable
    object WeatherBgList

    @Serializable
    data class WeatherBgEdit(
        val weatherType: String,
        val colorsJson: String = "",
        val nightColorsJson: String = "",
        val isEdit: Boolean = false,
        val isPreviewMode: Boolean = false
    )
}
