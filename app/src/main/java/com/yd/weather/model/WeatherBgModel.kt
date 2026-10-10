package com.yd.weather.model

import com.yd.weather.utils.DayPeriod
import kotlinx.serialization.Serializable

@Serializable
data class WeatherBgModel(
    val supportEdit: Boolean = false,
    val isSelected: Boolean = false,
    /** 下午（白天）的颜色；上午、傍晚没单独设过时也用它 */
    val colors: List<ULong>,
    val nightColors: List<ULong>,
    /**
     * 上午、傍晚单独设的颜色，null 表示跟随下午。
     * 只有用户自己加的背景会设；内置背景的分时段颜色在 WeatherBgUtils 的色表里，不进存储。
     * 默认值不会被写进存储，没设过的背景存下来和以前一模一样，旧版本也读得了
     */
    val morningColors: List<ULong>? = null,
    val duskColors: List<ULong>? = null,
)

/** 自己存的这几套颜色里，某个时段该用哪套 */
fun WeatherBgModel.colorsOf(period: DayPeriod): List<ULong> = when (period) {
    DayPeriod.MORNING -> morningColors ?: colors
    DayPeriod.AFTERNOON -> colors
    DayPeriod.DUSK -> duskColors ?: colors
    DayPeriod.NIGHT -> nightColors
}
