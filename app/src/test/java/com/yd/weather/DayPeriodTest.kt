package com.yd.weather

import com.yd.weather.utils.Commons
import com.yd.weather.utils.DayPeriod
import com.yd.weather.utils.DayPeriod.AFTERNOON
import com.yd.weather.utils.DayPeriod.DUSK
import com.yd.weather.utils.DayPeriod.MORNING
import com.yd.weather.utils.DayPeriod.NIGHT
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DayPeriodTest {

    private fun at(time: String): Date {
        val (h, m) = time.split(":").map { it.toInt() }
        return Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, h, m, 0)
        }.time
    }

    private fun period(time: String, sunrise: String?, sunset: String?): DayPeriod =
        Commons.dayPeriod(at(time), sunrise, sunset)

    @Test
    fun 杭州_正午取日出日落中点() {
        // 日出 06:00、日落 17:36，正午 11:48，傍晚从 16:36 起
        assertEquals(NIGHT, period("05:59", "06:00", "17:36"))
        assertEquals(MORNING, period("06:00", "06:00", "17:36"))
        assertEquals(MORNING, period("11:47", "06:00", "17:36"))
        assertEquals(AFTERNOON, period("11:48", "06:00", "17:36"))
        assertEquals(AFTERNOON, period("16:35", "06:00", "17:36"))
        assertEquals(DUSK, period("16:36", "06:00", "17:36"))
        assertEquals(DUSK, period("17:35", "06:00", "17:36"))
        assertEquals(NIGHT, period("17:36", "06:00", "17:36"))
    }

    @Test
    fun 乌鲁木齐_北京时间12点还是上午() {
        // 日出 08:35、日落 19:50，正午约 14:12
        assertEquals(MORNING, period("12:00", "08:35", "19:50"))
        assertEquals(MORNING, period("14:11", "08:35", "19:50"))
        assertEquals(AFTERNOON, period("14:13", "08:35", "19:50"))
    }

    @Test
    fun 日照短时傍晚不早于正午() {
        // 日出 07:30、日落 08:30：正午 08:00，日落前 60 分钟早于正午，所以傍晚从正午起，没有下午
        assertEquals(MORNING, period("07:59", "07:30", "08:30"))
        assertEquals(DUSK, period("08:00", "07:30", "08:30"))
    }

    @Test
    fun 没有日出日落数据按固定钟点() {
        assertEquals(NIGHT, period("05:59", null, null))
        assertEquals(MORNING, period("06:00", "", null))
        assertEquals(AFTERNOON, period("12:00", "bad", "bad"))
        assertEquals(DUSK, period("17:00", null, "18:00"))
        assertEquals(NIGHT, period("18:00", null, null))
    }

    @Test
    fun 夜间判定和以前一样() {
        // 以前的 isNight：当前分钟不在 [日出, 日落) 就是夜里；没数据时不在 [6, 18) 点
        for (minute in 0 until 24 * 60) {
            val time = "%02d:%02d".format(minute / 60, minute % 60)
            assertEquals(time, minute !in 360 until 1056, Commons.isNight(at(time), "06:00", "17:36"))
            assertEquals(time, minute / 60 !in 6 until 18, Commons.isNight(at(time)))
        }
    }
}
