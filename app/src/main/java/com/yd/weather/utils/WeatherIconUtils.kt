package com.yd.weather.utils

import androidx.annotation.DrawableRes
import com.yd.weather.R

object WeatherIconUtils {

    @DrawableRes
    fun getWeatherIconByType(type: Int, weatherType: String, isNight: Boolean): Int {
        // 优先按 weatherType 字符串匹配
        val byType = when (weatherType) {
            "CLEAR", "CLEAR_DAY", "CLEAR_NIGHT" ->
                if (isNight) R.mipmap.fifteen_weather_sunny_n else R.mipmap.fifteen_weather_sunny
            "PARTLY_CLOUDY", "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" ->
                if (isNight) R.mipmap.fifteen_weather_mostlycloudy_n else R.mipmap.fifteen_weather_mostlycloudy
            "CLOUDY" -> R.mipmap.fifteen_weather_cloudy
            "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE" -> R.mipmap.weather_icon_53
            "LIGHT_RAIN", "MODERATE_RAIN" -> R.mipmap.fifteen_weather_lightrain
            "HEAVY_RAIN", "STORM_RAIN" -> R.mipmap.fifteen_weather_rain
            "FOG" -> R.mipmap.weather_icon_18
            "LIGHT_SNOW" -> R.mipmap.weather_icon_14
            "MODERATE_SNOW" -> R.mipmap.weather_icon_15
            "HEAVY_SNOW" -> R.mipmap.weather_icon_16
            "STORM_SNOW" -> R.mipmap.weather_icon_17
            "DUST" -> R.mipmap.weather_icon_29
            "SAND" -> R.mipmap.weather_icon_30
            "WIND" -> R.mipmap.weather_icon_wind
            else -> null
        }
        if (byType != null) return byType

        // 按 type 数值兜底，昼夜有别
        val byTypeNum = if (!isNight) {
            when (type) {
                1 -> R.mipmap.fifteen_weather_sunny       // 晴天
                2 -> R.mipmap.fifteen_weather_mostlycloudy // 多云
                3 -> R.mipmap.fifteen_weather_chancerain   // 阵雨
                4 -> R.mipmap.fifteen_weather_chancestorm  // 雷阵雨
                else -> null
            }
        } else {
            when (type) {
                1 -> R.mipmap.fifteen_weather_sunny_n
                2 -> R.mipmap.fifteen_weather_mostlycloudy_n
                3 -> R.mipmap.fifteen_weather_chancerain_n
                4 -> R.mipmap.fifteen_weather_chancestorm_n
                else -> null
            }
        }
        if (byTypeNum != null) return byTypeNum

        // 昼夜通用的 type 兜底
        return when (type) {
            8, 9 -> R.mipmap.fifteen_weather_lightrain     // 小雨 / 小到中雨
            10, 11, 13 -> R.mipmap.fifteen_weather_rain   // 中雨 / 大雨 / 大暴雨
            34 -> R.mipmap.fifteen_weather_cloudy          // 阴
            else -> R.mipmap.fifteen_weather_no
        }
    }
}
