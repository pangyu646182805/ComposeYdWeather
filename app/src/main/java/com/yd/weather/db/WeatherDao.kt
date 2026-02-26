package com.yd.weather.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yd.weather.db.model.CityData
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM city_data ORDER BY `key` ASC")
    fun getCities(): Flow<List<CityData>>

    @Query("SELECT * FROM city_data WHERE isLocationCity = 1 LIMIT 1")
    suspend fun getLocationCity(): CityData?

    @Query("SELECT * FROM city_data WHERE cityid = :cityId LIMIT 1")
    suspend fun getCityByCityId(cityId: String): CityData?

    @Upsert
    suspend fun upsert(cityData: CityData)

    @Delete
    suspend fun delete(cityData: CityData)

    @Query("DELETE FROM city_data WHERE `key` = :key")
    suspend fun deleteById(key: Long)
}