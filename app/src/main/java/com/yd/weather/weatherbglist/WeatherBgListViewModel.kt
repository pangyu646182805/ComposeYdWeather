package com.yd.weather.weatherbglist

import com.yd.weather.app.AppState
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WeatherBgListViewModel @Inject constructor(
    navigator: AppNavigator,
    private val _appState: AppState
) : BaseViewModel(navigator, _appState) {

    private val _weatherBgMap = MutableStateFlow<Map<String, List<WeatherBgModel>>>(emptyMap())
    val weatherBgMap: StateFlow<Map<String, List<WeatherBgModel>>> = _weatherBgMap

    private val _isNight = MutableStateFlow(false)
    val isNight: StateFlow<Boolean> = _isNight

    private val _isShowMenu = MutableStateFlow(false)
    val isShowMenu: StateFlow<Boolean> = _isShowMenu

    init {
        loadWeatherBgMap()
    }

    fun loadWeatherBgMap() {
        _weatherBgMap.value = _appState.getPublicWeatherBgMap()
    }

    fun toggleNight() {
        _isNight.value = !_isNight.value
    }

    fun toggleMenu() {
        _isShowMenu.value = !_isShowMenu.value
    }

    fun hideMenu() {
        _isShowMenu.value = false
    }

    fun setCurrentWeatherBg(weatherType: String, model: WeatherBgModel) {
        _appState.setCurrentWeatherBg(weatherType, model)
        loadWeatherBgMap()
    }

    fun removeWeatherBg(weatherType: String, model: WeatherBgModel) {
        _appState.removeWeatherBg(weatherType, model)
        loadWeatherBgMap()
    }

    fun removeAllWeatherBg() {
        _appState.removeAllWeatherBg()
        _isShowMenu.value = false
        loadWeatherBgMap()
    }
}
