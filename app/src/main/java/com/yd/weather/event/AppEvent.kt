package com.yd.weather.event

sealed class AppEvent {
    data class RefreshWeatherDataEvent(val isAdd: Boolean) : AppEvent()
    object WeatherCardSortChangedEvent : AppEvent()
    object WeatherObservesCardSortChangedEvent : AppEvent()
    object WeatherBgMapChangedEvent : AppEvent()
    object WeatherBgChangedEvent : AppEvent()
}