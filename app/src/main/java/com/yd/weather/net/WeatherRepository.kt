package com.yd.weather.net

import com.yd.weather.db.model.CityData
import com.yd.weather.model.LocationData
import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherNetworkDataSource: WeatherNetworkDataSource
) {
    fun obtainCityList(): Flow<NetworkResponse<SelectCityData>> = flow {
        emit(weatherNetworkDataSource.obtainCityList())
    }.flowOn(Dispatchers.IO)

    fun obtainLocationDataByLocation(location: String): Flow<NetworkResponse<LocationData>> = flow {
        emit(weatherNetworkDataSource.obtainLocationDataByLocation(location))
    }.flowOn(Dispatchers.IO)

    fun searchCity(searchKey: String): Flow<NetworkResponse<List<CityData>>> = flow {
        emit(weatherNetworkDataSource.searchCity(searchKey))
    }.flowOn(Dispatchers.IO)
}