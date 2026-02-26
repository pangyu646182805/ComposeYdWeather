package com.yd.weather.db.model

import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherData
import com.yd.weather.utils.getToday
import com.yd.weather.utils.toDateString
import kotlinx.serialization.Serializable

@Serializable
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

fun emptySimpleWeatherData() = null.fromWeatherData()

fun WeatherData?.fromWeatherData(): SimpleWeatherData {
    val weatherData = this@fromWeatherData
    val currentWeatherDetailData =
        weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
    return SimpleWeatherData().copy(
        city = weatherData?.meta?.city ?: "",
        temp = weatherData?.observe?.temp ?: 0,
        tempHigh = currentWeatherDetailData?.high ?: 0,
        tempLow = currentWeatherDetailData?.low ?: 0,
        weatherType = weatherData?.observe?.thirdType ?: currentWeatherDetailData?.thirdType ?: "",
        weatherDesc = weatherData?.observe?.wthr ?: currentWeatherDetailData?.wthr ?: "",
        sunrise = currentWeatherDetailData?.sunrise ?: "",
        sunset = currentWeatherDetailData?.sunset ?: "",
    )
}