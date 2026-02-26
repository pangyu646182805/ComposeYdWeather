package com.yd.weather.viewmodel

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.drake.logcat.LogCat
import com.yd.weather.app.AppState
import com.yd.weather.config.Constants
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.CityData
import com.yd.weather.db.model.emptySimpleWeatherData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes
import com.yd.weather.utils.MMKVUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    private val weatherDbRepository: WeatherDbRepository,
) : BaseViewModel(navigator, appState) {
    init {
        handle()
    }

    private fun handle() {
        viewModelScope.launch {
            delay(800)
            var locationCity = weatherDbRepository.getLocationCity()
            LogCat.e("locationCity = $locationCity")
            if (locationCity == null) {
                val weatherData = emptySimpleWeatherData()
                locationCity = CityData().copy(
                    key = Constants.LOCATION_CITY_ID,
                    isLocationCity = true,
                    weatherData = weatherData,
                )
                weatherDbRepository.upsertCity(locationCity)
            }
            val currentCityId = MMKVUtils.getString(Constants.CURRENT_CITY_ID)
            if (!TextUtils.isEmpty(locationCity.cityId) || !TextUtils.isEmpty(currentCityId)) {
                val currentCity = weatherDbRepository.getCityByCityId(currentCityId)
                toMainPage()
            } else {
                toSelectCityPage()
            }
        }
    }

    private fun toMainPage() {
        navigateAndCloseCurrent(
            route = MainRoutes.Main,
            currentRoute = LaunchRoutes.Splash
        )
    }

    private fun toSelectCityPage() {
        navigateAndCloseCurrent(
            route = SelectCityRoutes.SelectCity,
            currentRoute = LaunchRoutes.Splash
        )
    }
}