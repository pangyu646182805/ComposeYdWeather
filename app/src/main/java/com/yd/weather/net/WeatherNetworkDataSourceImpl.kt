package com.yd.weather.net

import com.yd.weather.db.model.CityData
import com.yd.weather.model.LocationData
import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData
import javax.inject.Inject

class WeatherNetworkDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService
) : WeatherNetworkDataSource {
    override suspend fun obtainCityList(): NetworkResponse<SelectCityData> {
        return weatherService.obtainCityList()
    }

    override suspend fun obtainLocationDataByLocation(location: String): NetworkResponse<LocationData> {
        return weatherService.obtainLocationDataByLocation(location = location)
    }

    override suspend fun searchCity(searchKey: String): NetworkResponse<List<CityData>> {
        return weatherService.searchCity(searchKey = searchKey)
    }
}