package com.yd.weather.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Long (毫秒) 转 格式化字符串
 */
fun Long.toDateString(pattern: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Date.toDateString(pattern: String = "yyyy-MM-dd"): String {
    return this.time.toDateString(pattern)
}

fun getToday() = Date()

/**
 * 快速判断两个日期是否是同一天
 */
fun Date.isSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@isSameDay }
    val cal2 = Calendar.getInstance().apply { time = other }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * 增加或减少天数
 */
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DATE, days)
    return calendar.time
}