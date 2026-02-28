package com.yd.weather.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.config.Constants
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.fromWeatherData
import com.yd.weather.model.WeatherData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.ResultHandler
import com.yd.weather.net.WeatherRepository
import com.yd.weather.net.asResult
import com.yd.weather.routes.SelectCityRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    private val weatherRepository: WeatherRepository,
    private val weatherDbRepository: WeatherDbRepository,
) : BaseViewModel(navigator, appState) {
    private val _weatherBg = MutableStateFlow<List<Color>>(arrayListOf())
    val weatherBg: StateFlow<List<Color>> = _weatherBg

    private val _isWeatherHeaderDark = MutableStateFlow(false)
    val isWeatherHeaderDark: StateFlow<Boolean> = _isWeatherHeaderDark

    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark

    private val _panelOpacity = MutableStateFlow(0.1f)
    val panelOpacity: StateFlow<Float> = _panelOpacity

    private val _itemTypeObserves = MutableStateFlow<Array<Int>?>(null)
    val itemTypeObserves: StateFlow<Array<Int>?> = _itemTypeObserves

    private val _weatherItems = MutableStateFlow<List<WeatherItemData>?>(null)
    val weatherItems: StateFlow<List<WeatherItemData>?> = _weatherItems

    init {
        val weatherData = appState.currentCityData.value?.weatherData
        generateWeatherBg(null, weatherData?.weatherType, weatherData?.sunrise, weatherData?.sunset)
        obtainWeatherData()
    }

    fun obtainWeatherData() {
        val isLocationCity = appState.currentCityData.value?.isLocationCity ?: false
        val currentCityId = appState.currentCityData.value?.cityId ?: ""
        val key = if (isLocationCity) Constants.LOCATION_CITY_ID else currentCityId
        ResultHandler.handleResultWithT(
            scope = viewModelScope,
            flow = weatherRepository.obtainWeatherData(currentCityId).asResult(),
            onLoading = { setViewState(ViewState.Loading) },
            onData = { data ->
                setViewState(ViewState.Success)
                appState.saveWeatherData(key, data)
                setWeatherData(data)
            },
            onError = { _, _ ->
                setViewState(ViewState.Error)
            }
        )
    }

    private fun setWeatherData(weatherData: WeatherData?) {
        viewModelScope.launch {
            generateWeatherItems(weatherData)
            val currentCityData = appState.currentCityData.value
            if (currentCityData != null) {
                weatherDbRepository.updateWeatherData(
                    currentCityData,
                    weatherData.fromWeatherData()
                )
            }
        }
    }

    private fun generateWeatherItems(weatherData: WeatherData?) {
        // 生成天气背景
        _weatherBg.value = appState.generateWeatherBg(weatherData)
        // 根据天气背景计算天气头部是否是dark模式
        _isWeatherHeaderDark.value = appState.isWeatherHeaderDark(_weatherBg.value)
        // 根据天气背景计算天气内容是否是dark模式
        _isDark.value = appState.isDark(_weatherBg.value)
        // 根据天气背景计算天气面板的透明度
        _panelOpacity.value = appState.calPanelOpacity(_weatherBg.value)
        _itemTypeObserves.value = appState.getItemTypeObserves(
            appState.currentWeatherObservesCardSort.value,
            Constants.ITEM_TYPE_OBSERVE, weatherData
        )
        // 生成天气items数据
        _weatherItems.value = appState.generateWeatherItems(_itemTypeObserves.value, weatherData)
    }

    fun generateWeatherBg(
        weatherData: WeatherData?,
        cacheWeatherType: String?,
        cacheSunrise: String?,
        cacheSunset: String?
    ) {
        _weatherBg.value =
            appState.generateWeatherBg(weatherData, cacheWeatherType, cacheSunrise, cacheSunset)
        _isWeatherHeaderDark.value = appState.isWeatherHeaderDark(_weatherBg.value)
        _isDark.value = appState.isDark(_weatherBg.value)
        _panelOpacity.value = appState.calPanelOpacity(_weatherBg.value)
    }

    fun toSelectCityPage() {
        navigate(SelectCityRoutes.SelectCity)
    }
}