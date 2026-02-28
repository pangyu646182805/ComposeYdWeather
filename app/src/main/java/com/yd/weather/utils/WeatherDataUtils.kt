package com.yd.weather.utils

import androidx.compose.ui.graphics.Color
import com.yd.weather.model.WeatherData

object WeatherDataUtils {
    /*static generateWeatherBg(weatherData?: WeatherData, cacheWeatherType?: string, cacheSunrise?: string,
    cacheSunset?: string): Array<[ResourceColor, number]> {
        let weatherType = cacheWeatherType ?? (weatherData?.observe?.third_type ?? '')
        const currentWeatherDetailData = weatherData?.forecast15?.find(it => StrUtil.equal(it.date,
        DateUtil.getFormatDateStr(DateUtil.getToday(), Constants.YYYYMMDD)))
        if (StrUtil.isEmpty(weatherType)) {
            weatherType = currentWeatherDetailData?.third_type ?? ''
        }
        return WeatherBgUtils.generateWeatherBg(weatherType,
            isNight(DateUtil.getToday(), currentWeatherDetailData?.sunrise ?? cacheSunrise,
        currentWeatherDetailData?.sunset ?? cacheSunset), true)
    }*/

    fun generateWeatherBg(weatherData: WeatherData?, cacheWeatherType: String?, cacheSunrise: String?, cacheSunset: String?): List<Color> {
        return arrayListOf()
    }
}