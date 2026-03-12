package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    @SerialName("future_remind")
    val futureRemind: String? = null,
    val forecast15: List<WeatherDetailData>? = null,
    @SerialName("hourfc")
    val hourFc: List<WeatherHourData>? = null,
    val meta: WeatherMetaData? = null,
    val source: WeatherSourceData? = null,
    @SerialName("forecast40_v2")
    val forecast40V2: WeatherForecast40Data? = null,
    val forecast40: List<WeatherDetailData>? = null,
    val evn: WeatherEnvData? = null,
    val indexes: List<WeatherIndexData>? = null,
    val alarms: List<WeatherAlarmsData>? = null,
    val observe: WeatherObserveData? = null,
)
