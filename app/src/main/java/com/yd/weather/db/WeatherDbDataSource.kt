package com.yd.weather.db

import com.yd.weather.db.model.CityData
import com.yd.weather.db.model.SimpleWeatherData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDbDataSource @Inject constructor(
    private val weatherDao: WeatherDao
) {
    fun getCitiesFlow(): Flow<List<CityData>> = weatherDao.getCitiesFlow()

    suspend fun getCities(): List<CityData> = weatherDao.getCities()

    suspend fun getLocationCity(): CityData? = weatherDao.getLocationCity()

    suspend fun getCityByCityId(cityId: String): CityData? = weatherDao.getCityByCityId(cityId)

    suspend fun getCityByKey(key: String): CityData? = weatherDao.getCityByKey(key)

    suspend fun upsertCity(cityData: CityData) = weatherDao.upsert(cityData)

    suspend fun deleteCity(cityData: CityData) = weatherDao.delete(cityData)

    suspend fun deleteCities(cities: List<CityData>) = weatherDao.deleteCities(cities)

    suspend fun deleteCityById(id: Long) = weatherDao.deleteById(id)

    suspend fun updateWeatherData(cityData: CityData, weatherData: SimpleWeatherData) {
        weatherDao.upsert(cityData.copy(weatherData = weatherData))
    }
}