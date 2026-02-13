package com.yd.weather.net

import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData

interface WeatherNetworkDataSource {
    suspend fun obtainCityList(): NetworkResponse<SelectCityData>
}