package com.yd.weather.net

import com.yd.weather.db.model.CityData
import com.yd.weather.model.LocationData
import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData

interface WeatherNetworkDataSource {
    suspend fun obtainCityList(): NetworkResponse<SelectCityData>

    suspend fun obtainLocationDataByLocation(location: String): NetworkResponse<LocationData>

    suspend fun searchCity(searchKey: String): NetworkResponse<List<CityData>>
}