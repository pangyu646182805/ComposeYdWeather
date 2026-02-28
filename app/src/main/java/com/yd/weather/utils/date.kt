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
 * 根据字符串内容推断日期格式：
 *   8位纯数字  → yyyyMMdd    (e.g. "20250228")
 *   10位纯数字 → yyyyMMddHH  (e.g. "2025022806")
 *   HH:mm      → HH:mm       (e.g. "06:30")
 */
private fun inferDatePattern(dateStr: String): String? = when {
    dateStr.matches(Regex("""\d{8}""")) -> "yyyyMMdd"
    dateStr.matches(Regex("""\d{10}""")) -> "yyyyMMddHH"
    dateStr.matches(Regex("""\d{12}""")) -> "yyyyMMddHHmm"
    dateStr.matches(Regex("""\d{2}:\d{2}""")) -> "HH:mm"
    else -> null
}

/**
 * 日期字符串格式转换，自动推断 fromPattern，按 toPattern 格式化输出
 */
fun formatDateStr(dateStr: String?, toPattern: String): String? {
    if (dateStr.isNullOrEmpty()) return null
    val fromPattern = inferDatePattern(dateStr) ?: return null
    return try {
        val from = SimpleDateFormat(fromPattern, Locale.getDefault())
        val to = SimpleDateFormat(toPattern, Locale.getDefault())
        val date = from.parse(dateStr) ?: return null
        to.format(date)
    } catch (_: Exception) {
        null
    }
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