package com.yd.weather.utils

import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData

object AppRuntimeData {
    private var currentCityData: CityData? = null

    fun getCurrentCityData() = currentCityData

    fun setCurrentCityData(cityData: CityData?) {
        this.currentCityData = cityData
        if (cityData != null) {
            MMKVUtils.putString(
                Constants.CURRENT_CITY_ID,
                if (cityData.isLocationCity) Constants.LOCATION_CITY_ID else cityData.cityId
            )
        }
    }
}