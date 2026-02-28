package com.yd.weather.utils

import java.util.Calendar
import java.util.Date

object Commons {
    fun isNight(
        dateTime: Date?,
        sunrise: String? = null,
        sunset: String? = null,
    ): Boolean {
        if (dateTime == null) return false
        if (!sunrise.isNullOrEmpty() && sunrise.contains(":") &&
            !sunset.isNullOrEmpty() && sunset.contains(":")
        ) {
            return isNightByRange(dateTime, sunrise, sunset)
        }
        val hour = dateTime.toCalendar().get(Calendar.HOUR_OF_DAY)
        return hour !in 6..<18
    }

    private fun isNightByRange(
        dateTime: Date,
        sunrise: String,
        sunset: String,
    ): Boolean {
        val cal = dateTime.toCalendar()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val sunriseMinutes = sunrise.toTotalMinutes()
        val sunsetMinutes = sunset.toTotalMinutes()

        return currentMinutes !in sunriseMinutes..<sunsetMinutes
    }

    private fun Date.toCalendar(): Calendar = Calendar.getInstance().also { it.time = this }

    private fun String.toTotalMinutes(): Int {
        val parts = split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    fun isSunriseOrSunset(sunriseOrSunset: String?, time: String?): Boolean {
        if (sunriseOrSunset.isNullOrEmpty() || time.isNullOrEmpty()) return false
        val formatDateStr = formatDateStr(time, "HH") ?: return false
        return sunriseOrSunset.startsWith(formatDateStr)
    }
}
