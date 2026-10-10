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
        // 空表示跟随下午
        val morningColorsJson: String = "",
        val duskColorsJson: String = "",
        // 列表页当前看的是哪个时段，编辑页就先打开哪个 tab（DayPeriod.name）
        val period: String = "",
        val isEdit: Boolean = false,
        val isPreviewMode: Boolean = false
    )
}
