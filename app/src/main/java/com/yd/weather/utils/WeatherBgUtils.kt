package com.yd.weather.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.yd.weather.R

object WeatherBgUtils {
    fun fixedWeatherType(weatherType: String): String {
        return when (weatherType) {
            "CLEAR", "CLEAR_DAY", "CLEAR_NIGHT" -> "CLEAR"
            "PARTLY_CLOUDY", "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" -> "PARTLY_CLOUDY"
            "CLOUDY" -> "CLOUDY"
            "LIGHT_HAZE", "MODERATE_HAZE" -> "LIGHT_HAZE"
            "HEAVY_HAZE" -> "HEAVY_HAZE"
            "LIGHT_RAIN" -> "LIGHT_RAIN"
            "MODERATE_RAIN", "HEAVY_RAIN", "STORM_RAIN" -> "MODERATE_RAIN"
            "FOG" -> "FOG"
            "LIGHT_SNOW", "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" -> "LIGHT_SNOW"
            "DUST", "SAND" -> "DUST"
            "WIND" -> "WIND"
            else -> "CLEAR"
        }
    }

    fun generateWeatherBg(context: Context, type: String, isDark: Boolean): List<Int> {
        var color1 = if (isDark) R.color.color_1a1b30 else R.color.color_f47359
        var color2 = if (isDark) R.color.color_2e3c54 else R.color.color_f1ab80
        when (type) {
            "CLEAR", "CLEAR_DAY", "CLEAR_NIGHT" -> {
                color1 = if (isDark) R.color.color_1a1b30 else R.color.color_f47359
                color2 = if (isDark) R.color.color_2e3c54 else R.color.color_f1ab80
            }

            "PARTLY_CLOUDY", "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" -> {
                color1 = if (isDark) R.color.color_2e336c else R.color.color_abb7c4
                color2 = if (isDark) R.color.color_64648d else R.color.color_b6c7cd
            }

            "CLOUDY" -> {
                color1 = if (isDark) R.color.color_1e2232 else R.color.color_58677f
                color2 = if (isDark) R.color.color_354359 else R.color.color_828d9e
            }

            "LIGHT_HAZE", "MODERATE_HAZE" -> {
                color1 = R.color.color_bc8e3e
                color2 = R.color.color_e5bb62
            }

            "HEAVY_HAZE" -> {
                color1 = R.color.color_b77b32
                color2 = R.color.color_f7ba66
            }

            "LIGHT_RAIN" -> {
                color1 = if (isDark) R.color.color_171a2a else R.color.color_5e738d
                color2 = if (isDark) R.color.color_3c4354 else R.color.color_8f9aad
            }

            "MODERATE_RAIN", "HEAVY_RAIN", "STORM_RAIN" -> {
                color1 = R.color.color_171a2a
                color2 = R.color.color_3c4354
            }

            "FOG", "LIGHT_SNOW", "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" -> {
                color1 = R.color.color_abb7c4
                color2 = R.color.color_b6c7cd
            }

            "DUST", "SAND" -> {
                color1 = R.color.color_f7cb6a
                color2 = R.color.color_fda085
            }

            "WIND" -> {
                color1 = R.color.color_4776b0
                color2 = R.color.color_e9a4b4
            }
        }
        return arrayListOf(
            ContextCompat.getColor(context, color1),
            ContextCompat.getColor(context, color2)
        )
    }
}