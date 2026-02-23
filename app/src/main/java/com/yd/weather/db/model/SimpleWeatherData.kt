package com.yd.weather.db.model

data class SimpleWeatherData(
    val city: String = "",
    val temp: Int = 0,
    val tempHigh: Int = 0,
    val tempLow: Int = 0,
    val weatherType: String = "",
    val weatherDesc: String = "",
    val sunrise: String = "",
    val sunset: String = "",
)
