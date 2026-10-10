package com.yd.weather.utils

import androidx.compose.ui.graphics.Color

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

    /** 内置背景在某个时段的渐变，第一个颜色在屏幕顶部 */
    fun generateWeatherBg(type: String, period: DayPeriod): List<Color> {
        val palette = PALETTE.getValue(fixedWeatherType(type))
        return when (period) {
            DayPeriod.MORNING -> palette.morning
            DayPeriod.AFTERNOON -> palette.afternoon
            DayPeriod.DUSK -> palette.dusk
            DayPeriod.NIGHT -> palette.night
        }
    }

    private class Palette(
        val day: List<Color>,
        val night: List<Color>,
        val morning: List<Color> = day,
        val afternoon: List<Color> = day,
        val dusk: List<Color> = day,
    )

    private fun gradient(top: Long, bottom: Long) = listOf(Color(top), Color(bottom))

    /**
     * 每一组都按字真正落在的位置量过，浅色、深色模式各算一遍：
     * 页头文字（屏幕顶部到 36%）和城市卡片文字（卡片 28%~72%）对比 ≥ 4.5，
     * 面板白字（屏幕 35% 到底，算上面板叠的白/黑）对比 ≥ 3；
     * 同一时段同屏的两种天气，整条渐变平均色差 ≥ 12（雪是例外，见下）。
     *
     * 面板文字写死是白色，所以白天只有两种可行的配法：
     * 整条偏中深配白字（阴、霾、雨、大风、晴下午），或者落在很窄的浅中间调配黑字
     * （晴上午、多云、雾、雪、沙尘）。调色时别把底色调亮，面板字会看不清。
     *
     * 只有看得见太阳的天气才分时段：晴分上午、下午、傍晚，多云多一套傍晚。
     */
    private val PALETTE = mapOf(
        "CLEAR" to Palette(
            day = gradient(0xFF2B67BE, 0xFF1C69AD),
            morning = gradient(0xFF8BC9F1, 0xFF73B1CC),
            afternoon = gradient(0xFF2B67BE, 0xFF1C69AD),
            // 头顶已经暗成暮蓝紫，暖色只留在下面一点
            dusk = gradient(0xFF5F5F9F, 0xFF8D555C),
            night = gradient(0xFF0F1A3A, 0xFF2B4170),
        ),
        "PARTLY_CLOUDY" to Palette(
            day = gradient(0xFFB0C3DB, 0xFF96ABBD),
            dusk = gradient(0xFF6B5E7C, 0xFF7C5C54),
            night = gradient(0xFF353456, 0xFF55557A),
        ),
        "CLOUDY" to Palette(
            day = gradient(0xFF515A61, 0xFF4A565C),
            night = gradient(0xFF2A2B2F, 0xFF3A3C40),
        ),
        "LIGHT_HAZE" to Palette(
            day = gradient(0xFF706242, 0xFF716146),
            night = gradient(0xFF2E281F, 0xFF5C503C),
        ),
        "HEAVY_HAZE" to Palette(
            day = gradient(0xFF4E4232, 0xFF463B2E),
            night = gradient(0xFF1E1610, 0xFF2C2219),
        ),
        "LIGHT_RAIN" to Palette(
            day = gradient(0xFF366688, 0xFF365C87),
            night = gradient(0xFF182C42, 0xFF2A4560),
        ),
        "MODERATE_RAIN" to Palette(
            day = gradient(0xFF2F3C55, 0xFF34425A),
            night = gradient(0xFF0D121C, 0xFF232E40),
        ),
        "FOG" to Palette(
            day = gradient(0xFFC3C2BA, 0xFFAAA8A3),
            night = gradient(0xFF4A4A48, 0xFF605F5C),
        ),
        // 银白：最像下雪天。和多云、雾只差 8~9，挨着时靠冷暖区分，为了像雪特意放宽
        "LIGHT_SNOW" to Palette(
            day = gradient(0xFFB9C1C9, 0xFFA5AEB6),
            night = gradient(0xFF505A64, 0xFF5B656F),
        ),
        "DUST" to Palette(
            day = gradient(0xFFE4BC8A, 0xFFD29D7A),
            night = gradient(0xFF43281A, 0xFF7A4A2E),
        ),
        // 钢青：偏冷的青蓝，刮冷风时的天色
        "WIND" to Palette(
            day = gradient(0xFF346C7E, 0xFF376C79),
            night = gradient(0xFF385B67, 0xFF496972),
        ),
    )
}
