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
        return dayPeriod(dateTime, sunrise, sunset) == DayPeriod.NIGHT
    }

    /**
     * 按当天日出日落把一天分成四段：
     * - 夜间：日落 ~ 次日日出（和以前的 [isNight] 一样）
     * - 上午：日出 ~ 正午
     * - 下午：正午 ~ 日落前 [DUSK_MINUTES] 分钟
     * - 傍晚：之后到日落
     *
     * 正午取日出日落的中点，不写死 12 点：全国都用北京时间，乌鲁木齐的正午在 14 点左右，
     * 哈尔滨在 11 点出头，写死的话新疆中午 12 点就进了「下午」。
     * 日照短的时候傍晚不早于正午，下午被压短甚至没有。
     * 没有日出日落数据时按 6、12、17、18 点切。
     */
    fun dayPeriod(
        dateTime: Date,
        sunrise: String? = null,
        sunset: String? = null,
    ): DayPeriod {
        val cal = dateTime.toCalendar()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val rise = sunrise.toTotalMinutesOrNull()
        val set = sunset.toTotalMinutesOrNull()
        if (rise == null || set == null || set <= rise) {
            return when (now / 60) {
                in 6..<12 -> DayPeriod.MORNING
                in 12..<17 -> DayPeriod.AFTERNOON
                in 17..<18 -> DayPeriod.DUSK
                else -> DayPeriod.NIGHT
            }
        }
        if (now !in rise..<set) return DayPeriod.NIGHT
        val noon = (rise + set) / 2
        val duskStart = maxOf(noon, set - DUSK_MINUTES)
        return when {
            now < noon -> DayPeriod.MORNING
            now < duskStart -> DayPeriod.AFTERNOON
            else -> DayPeriod.DUSK
        }
    }

    /** 傍晚从日落前多久开始 */
    private const val DUSK_MINUTES = 60

    private fun String?.toTotalMinutesOrNull(): Int? {
        if (isNullOrEmpty() || !contains(":")) return null
        return toTotalMinutes()
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
