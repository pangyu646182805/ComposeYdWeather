package com.yd.weather.net

import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData
import javax.inject.Inject

class WeatherNetworkDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService
) : WeatherNetworkDataSource {
    override suspend fun obtainCityList(): NetworkResponse<SelectCityData> {
        return weatherService.obtainCityList()
    }
}