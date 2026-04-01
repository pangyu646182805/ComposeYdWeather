package com.yd.weather.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.ResultHandler
import com.yd.weather.net.WeatherRepository
import com.yd.weather.net.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WeatherPreviewViewModel @Inject constructor(
    navigator: AppNavigator,
    private val _appState: AppState,
    private val weatherRepository: WeatherRepository,
) : BaseViewModel(navigator, _appState) {
    var cityId: String? = null
        private set

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

    fun appState(): AppState = _appState

    /**
     * 由 Composable 调用，传入路由参数并触发初始化
     */
    fun initialize(cityId: String?) {
        if (_weatherBg.value.isNotEmpty()) return
        this.cityId = cityId
        val weatherData = appState.currentCityData.value?.weatherData
        generateWeatherBg(null, weatherData?.weatherType, weatherData?.sunrise, weatherData?.sunset)
        obtainWeatherData()
    }

    fun obtainWeatherData() {
        ResultHandler.handleResultWithT(
            scope = viewModelScope,
            flow = weatherRepository.obtainWeatherData(cityId ?: "").asResult(),
            onLoading = { setViewState(ViewState.Loading) },
            onData = { data ->
                setViewState(ViewState.Success)
                generateWeatherItems(data)
            },
            onError = { _, _ ->
                // setViewState(ViewState.Error)
            }
        )
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

    fun refreshWeatherData(onComplete: () -> Unit) {
        ResultHandler.handleResultWithT(
            scope = viewModelScope,
            delayTimeMillis = 400,
            flow = weatherRepository.obtainWeatherData(cityId ?: "").asResult(),
            onLoading = {},
            onData = { data ->
                setViewState(ViewState.Success)
                generateWeatherItems(data)
                onComplete()
            },
            onError = { _, _ ->
                onComplete()
            }
        )
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
}