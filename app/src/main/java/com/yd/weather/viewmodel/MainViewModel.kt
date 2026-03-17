package com.yd.weather.viewmodel

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.drake.logcat.LogCat
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.config.Constants
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.CityData
import com.yd.weather.db.model.fromWeatherData
import com.yd.weather.model.WeatherData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.ResultHandler
import com.yd.weather.net.WeatherRepository
import com.yd.weather.net.asResult
import com.yd.weather.utils.CoordinateConverter
import com.yd.weather.utils.LocationProvider
import com.yd.weather.utils.MMKVUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    navigator: AppNavigator,
    private val _appState: AppState,
    private val weatherRepository: WeatherRepository,
    private val weatherDbRepository: WeatherDbRepository,
    @param:ApplicationContext private val context: Context
) : BaseViewModel(navigator, _appState) {
    private val _isShowWeatherPage = MutableStateFlow(true)
    val isShowWeatherPage: StateFlow<Boolean> = _isShowWeatherPage

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

    private val _addedCityData = MutableStateFlow<List<CityData>?>(null)
    val addedCityData: StateFlow<List<CityData>?> = _addedCityData

    private var hasCheckLocationCity = false

    var offsetY = 0f

    private val _predictiveBackProgress = MutableStateFlow<Float?>(null)
    val predictiveBackProgress: StateFlow<Float?> = _predictiveBackProgress

    fun updatePredictiveBackProgress(progress: Float?) {
        _predictiveBackProgress.value = progress
    }

    fun appState(): AppState = _appState

    init {
        val weatherData = appState.currentCityData.value?.weatherData
        generateWeatherBg(null, weatherData?.weatherType, weatherData?.sunrise, weatherData?.sunset)
        obtainWeatherData()
        viewModelScope.launch {
            appState.currentCityData.drop(1).collect {
                println("obtainWeatherData obtainWeatherData")
                obtainWeatherData()
            }
        }
    }

    fun obtainWeatherData() {
        val isLocationCity = appState.currentCityData.value?.isLocationCity ?: false
        val currentCityId = appState.currentCityData.value?.cityId ?: ""
        val key = if (isLocationCity) Constants.LOCATION_CITY_ID else currentCityId
        val weatherData = appState.getWeatherData(key)
        if (weatherData != null) {
            setViewState(ViewState.Success)
            setWeatherData(weatherData)
        }
        ResultHandler.handleResultWithT(
            scope = viewModelScope,
            flow = weatherRepository.obtainWeatherData(currentCityId).asResult(),
            onLoading = {
                if (weatherData == null) setViewState(ViewState.Loading)
            },
            onData = { data ->
                setViewState(ViewState.Success)
                appState.saveWeatherData(key, data)
                setWeatherData(data)
                checkLocationCity { reObtainWeatherData ->
                    if (reObtainWeatherData) {
                        obtainWeatherData()
                    }
                }
            },
            onError = { _, _ ->
                // setViewState(ViewState.Error)
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
            obtainAddedCityData()
        }
    }

    private fun obtainAddedCityData() {
        viewModelScope.launch {
            val cities = weatherDbRepository.getCities()
            val currentCityIdList = MMKVUtils.getStringSet(Constants.CURRENT_CITY_ID_LIST)
            val list = arrayListOf<CityData>()
            currentCityIdList.forEach { cityId ->
                val find = cities.find { it.key == cityId }
                if (find != null) {
                    if (cityId == Constants.LOCATION_CITY_ID) {
                        list.add(0, find)
                    } else {
                        list.add(find)
                    }
                }
            }
            _addedCityData.value = list
        }
    }

    private fun checkLocationCity(block: (reObtainWeatherData: Boolean) -> Unit) {
        if (!hasCheckLocationCity && appState.currentCityData.value?.isLocationCity ?: false) {
            viewModelScope.launch {
                val location = withTimeoutOrNull(10000L) {
                    LocationProvider(context).fetchSingleLocation()
                }
                if (location != null) {
                    hasCheckLocationCity = true
                    val transform = CoordinateConverter.wgs84ToGcj02(location.longitude, location.latitude)
                    ResultHandler.handleResultWithData(
                        scope = viewModelScope,
                        flow = weatherRepository.obtainLocationDataByLocation("${transform[1]},${transform[0]}")
                            .asResult(),
                        showToast = false,
                        onData = { data ->
                            val province = data.addressComponent?.province ?: ""
                            if (appState.currentCityData.value?.name ==
                                data.addressComponent?.district &&
                                appState.currentCityData.value?.street ==
                                data.addressComponent?.street &&
                                (province.contains(appState.currentCityData.value?.prov ?: "") ||
                                        (appState.currentCityData.value?.prov ?: "").contains(
                                            province
                                        ))
                            ) {
                                LogCat.e("定位位置相同")
                            } else {
                                searchCity(data.addressComponent?.district ?: "") { result ->
                                    if (result.isNotEmpty()) {
                                        val find = result.find {
                                            it.name == data.addressComponent?.district && (province.contains(
                                                it.prov ?: ""
                                            ) || (it.prov ?: "").contains(province))
                                        }
                                        if (find != null) {
                                            val cityData = find.copy(
                                                key = Constants.LOCATION_CITY_ID,
                                                isLocationCity = true,
                                                street = data.addressComponent?.street
                                            )
                                            viewModelScope.launch {
                                                weatherDbRepository.upsertCity(cityData)
                                                appState.setCurrentCityData(cityData)
                                                block.invoke(true)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    private fun searchCity(searchKey: String, block: ((List<CityData>) -> Unit)? = null) {
        ResultHandler.handleResultWithData(
            scope = viewModelScope,
            flow = weatherRepository.searchCity(searchKey).asResult(),
            showToast = false,
            onData = { data ->
                block?.invoke(data)
            },
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

    fun swapAddedCityData(fromIndex: Int, toIndex: Int) {
        val addedCityData = _addedCityData.value
        if (addedCityData.isNullOrEmpty()) return
        addedCityData.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
            _addedCityData.value = this
        }
    }

    fun onSwapDragStopped() {
        val currentCityIdList = _addedCityData.value?.map {
            if (it.isLocationCity) Constants.LOCATION_CITY_ID else it.cityId ?: ""
        }
        if (!currentCityIdList.isNullOrEmpty()) {
            MMKVUtils.putStringSet(Constants.CURRENT_CITY_ID_LIST, currentCityIdList.toMutableSet())
        }
    }

    fun removeCityData(cityData: CityData?, block: () -> Unit) {
        if (cityData == null) return
        val addedCityData = _addedCityData.value
        if (addedCityData.isNullOrEmpty()) return
        viewModelScope.launch {
            weatherDbRepository.deleteCity(cityData)
            _addedCityData.value = addedCityData.filter { it.cityId != cityData.cityId }
            block()
        }
    }

    fun removeCities(cities: List<CityData>?, block: () -> Unit) {
        if (cities.isNullOrEmpty()) return
        val addedCityData = _addedCityData.value
        if (addedCityData.isNullOrEmpty()) return
        viewModelScope.launch {
            weatherDbRepository.deleteCities(cities)
            _addedCityData.value = addedCityData.filter { !cities.contains(it) }
            block()
        }
    }

    fun showCityManagerPage(cityManagerViewModel: CityManagerViewModel, cityManagerScrollState: LazyListState) {
        val addedCityData = _addedCityData.value
        if (addedCityData.isNullOrEmpty()) return
        val currentCityData = appState.currentCityData.value ?: return
        val fullyVisibleIndices = cityManagerViewModel.fullyVisibleIndices
        viewModelScope.launch {
            val index = addedCityData.indexOfFirst { it.cityId == currentCityData.cityId }
            if (index >= 0) {
                if (!fullyVisibleIndices.contains(index + 1)) {
                    cityManagerScrollState.scrollToItem(index + 1)
                }
                cityManagerScrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index + 1 }?.let { itemInfo ->
                    this@MainViewModel.offsetY = itemInfo.offset + cityManagerViewModel.listOffsetY
                }
                _isShowWeatherPage.value = false
            }
        }
    }

    fun showWeatherPage(cityManagerViewModel: CityManagerViewModel, cityManagerScrollState: LazyListState) {
        val addedCityData = _addedCityData.value
        if (addedCityData.isNullOrEmpty()) return
        val currentCityData = appState.currentCityData.value ?: return
        viewModelScope.launch {
            val index = addedCityData.indexOfFirst { it.cityId == currentCityData.cityId }
            if (index >= 0) {
                val itemInfo = cityManagerScrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index + 1 }
                if (itemInfo != null) {
                    this@MainViewModel.offsetY = itemInfo.offset + cityManagerViewModel.listOffsetY
                }
                _isShowWeatherPage.value = true
            }
        }
    }
}