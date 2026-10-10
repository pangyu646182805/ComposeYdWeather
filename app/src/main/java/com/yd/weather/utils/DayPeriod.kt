package com.yd.weather.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * 一天里的四个时段，天气背景按它换色（见 [WeatherBgUtils]）。
 * 怎么划分见 [Commons.dayPeriod]。
 */
enum class DayPeriod(val label: String) {
    MORNING("上午"),
    AFTERNOON("下午"),
    DUSK("傍晚"),
    NIGHT("夜间");

    /** 天气背景列表页右上角按上午、下午、傍晚、夜间循环切换 */
    fun next(): DayPeriod = entries[(ordinal + 1) % entries.size]
}

/**
 * 某个城市当前的时段，时钟到点会自动变。
 *
 * [clock] 只当触发器用：它每分钟走一次，这里跟着重算；但只有时段真的变了，
 * 读它的地方才会重组，平时一整天只在四个切换点各变一次。
 */
@Composable
fun rememberDayPeriod(clock: StateFlow<Long>, sunrise: String?, sunset: String?): DayPeriod {
    val tick = clock.collectAsStateWithLifecycle()
    val period by remember(sunrise, sunset) {
        derivedStateOf {
            tick.value
            Commons.dayPeriod(getToday(), sunrise, sunset)
        }
    }
    return period
}
