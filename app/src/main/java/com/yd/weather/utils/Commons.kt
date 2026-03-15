package com.yd.weather.utils

import com.yd.weather.R
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun getTemp(temp: Int?) = if (temp == null) "" else "${temp}°"

    /**
     * 获取小时天气显示文字：
     * - 非当前小时 → "HH时"
     * - 当前小时，且日出/日落时刻与当前小时重叠（已过该时刻）→ "HH时"
     * - 其余当前小时 → "现在"
     */
    fun getWeatherHourTime(time: String?, sunrise: String?, sunset: String?): String {
        if (!isHourNow(time)) {
            return formatDateStr(time, "HH时") ?: ""
        }
        val sunriseHM = parseHourMinute(sunrise)
        val sunsetHM = parseHourMinute(sunset)
        if (sunriseHM != null && sunsetHM != null) {
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val sunriseCal = calendarOfToday(sunriseHM[0], sunriseHM[1])
            val sunsetCal = calendarOfToday(sunsetHM[0], sunsetHM[1])
            if (sunriseHM[0] == currentHour && now.timeInMillis > sunriseCal.timeInMillis) {
                return formatDateStr(time, "HH时") ?: ""
            }
            if (sunsetHM[0] == currentHour && now.timeInMillis > sunsetCal.timeInMillis) {
                return formatDateStr(time, "HH时") ?: ""
            }
        }
        return "现在"
    }

    /** 判断 time 字符串（yyyyMMddHH）是否为当前小时 */
    private fun isHourNow(time: String?): Boolean {
        if (time.isNullOrEmpty()) return false
        val timeHour = formatDateStr(time, "yyyyMMddHH") ?: return false
        val nowHour = java.text.SimpleDateFormat("yyyyMMddHH", Locale.getDefault()).format(Date())
        return timeHour == nowHour
    }

    /** 解析 "HH:mm" → [hour, minute]，格式不合法返回 null */
    private fun parseHourMinute(timeStr: String?): IntArray? {
        if (timeStr.isNullOrEmpty() || !timeStr.contains(":")) return null
        val parts = timeStr.split(":")
        if (parts.size < 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return intArrayOf(hour, minute)
    }

    private fun calendarOfToday(hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private val WEEK_DAYS = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    fun getWeatherDateTime(date: String?): String {
        if (date.isNullOrEmpty()) return ""
        val dateTime = getFormatDate(date)
        val today = getToday()
        return when {
            dateTime.isSameDay(today.addDays(-1)) -> "昨天"
            dateTime.isSameDay(today) -> "今天"
            dateTime.isSameDay(today.addDays(1)) -> "明天"
            else -> {
                val cal = Calendar.getInstance().apply { time = dateTime }
                WEEK_DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]
            }
        }
    }

    fun isBefore(date: String?): Boolean {
        if (date.isNullOrEmpty()) return false
        val dateTime = getFormatDate(date)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        return dateTime.before(todayStart)
    }

    fun getAqiColor(aqi: Int?): Int {
        val value = aqi ?: 0
        return when {
            value <= 50 -> R.color.color_00e301
            value <= 100 -> R.color.color_fdfd01
            value <= 150 -> R.color.color_fd7e01
            value <= 200 -> R.color.color_f70001
            value <= 300 -> R.color.color_98004c
            else -> R.color.color_7d0023
        }
    }
}
