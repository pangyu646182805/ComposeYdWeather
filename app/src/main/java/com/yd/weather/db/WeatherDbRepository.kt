package com.yd.weather.db

import com.yd.weather.db.model.CityData
import com.yd.weather.db.model.SimpleWeatherData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDbRepository @Inject constructor(
    private val weatherDataSource: WeatherDbDataSource
) {
    fun getCities(): Flow<List<CityData>> = weatherDataSource.getCities()

    suspend fun getLocationCity(): CityData? = weatherDataSource.getLocationCity()

    suspend fun getCityByCityId(cityId: String): CityData? = weatherDataSource.getCityByCityId(cityId)

    suspend fun upsertCity(cityData: CityData) = weatherDataSource.upsertCity(cityData)

    suspend fun deleteCity(cityData: CityData) = weatherDataSource.deleteCity(cityData)

    suspend fun deleteCityById(id: Long) = weatherDataSource.deleteCityById(id)

    suspend fun updateWeatherData(cityData: CityData, weatherData: SimpleWeatherData) =
        weatherDataSource.updateWeatherData(cityData, weatherData)
}