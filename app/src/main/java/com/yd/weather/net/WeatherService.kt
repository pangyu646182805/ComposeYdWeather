package com.yd.weather.net

import com.yd.weather.model.NetworkResponse
import com.yd.weather.model.SelectCityData
import retrofit2.http.GET
import retrofit2.http.Url

interface WeatherService {
    @GET
    suspend fun obtainCityList(@Url url: String = Api.SELECT_CITY_API): NetworkResponse<SelectCityData>
}