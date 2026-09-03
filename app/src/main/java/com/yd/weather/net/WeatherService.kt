package com.yd.weather.net

import com.yd.weather.db.model.CityData
import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData
import com.yd.weather.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherService {
    @GET
    suspend fun obtainCityList(@Url url: String = Api.SELECT_CITY_API): NetworkResponse<SelectCityData>

    @GET
    suspend fun searchCity(
        @Url url: String = Api.SEARCH_CITY_API,
        @Query("keyword") searchKey: String
    ): NetworkResponse<List<CityData>>

    @GET
    suspend fun obtainWeatherData(
        @Url url: String = Api.WEATHER_API,
        @Query("citykey") cityId: String
    ): WeatherData
}