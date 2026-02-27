package com.yd.weather.viewmodel

import android.content.Context
import android.location.Location
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.drake.logcat.LogCat
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.config.Constants
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.CityData
import com.yd.weather.db.model.emptySimpleWeatherData
import com.yd.weather.model.LocationData
import com.yd.weather.model.SelectCityData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.MainRoutes
import com.yd.weather.net.ResultHandler
import com.yd.weather.net.WeatherRepository
import com.yd.weather.net.asResult
import com.yd.weather.routes.WeatherPreviewRoutes
import com.yd.weather.utils.AppRuntimeData
import com.yd.weather.utils.LocationProvider
import com.yd.weather.utils.MMKVUtils
import com.yd.weather.utils.PermissionUtils
import com.yd.weather.utils.ToastUtils
import com.yd.weather.weatherpreview.WeatherPreviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class SelectCityViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    private val weatherRepository: WeatherRepository,
    private val weatherDbRepository: WeatherDbRepository,
) : BaseViewModel(navigator, appState) {
    init {
        loadCityList()
    }

    private val _selectCityData = MutableStateFlow<SelectCityData?>(null)
    val selectCityData: StateFlow<SelectCityData?> = _selectCityData

    private val _cities = weatherDbRepository.getCities()
    val cities = _cities.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = arrayListOf(),
    )

    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData

    // 0-定位中
    // 1-定位结束
    private val _locationState = MutableStateFlow(0)
    val locationState: StateFlow<Int> = _locationState

    private val _searchResult = MutableStateFlow<List<CityData>?>(null)
    val searchResult: StateFlow<List<CityData>?> = _searchResult

    fun loadCityList(delayTimeMillis: Long? = null) {
        ResultHandler.handleResultWithData(
            scope = viewModelScope,
            flow = weatherRepository.obtainCityList().asResult(),
            delayTimeMillis = delayTimeMillis,
            onLoading = { setViewState(ViewState.Loading) },
            onData = { data ->
                setViewState(ViewState.Success)
                _selectCityData.value = data
            },
            onError = { _, _ ->
                setViewState(ViewState.Error)
            }
        )
    }

    fun obtainLocationPermission(context: Context) {
        if (_locationState.value == 1) return
        _locationState.value = 0
        PermissionUtils.requestLocationPermission(context) { granted ->
            if (granted) {
                onPermissionGranted(context)
            } else {
                _locationData.value = null
                _locationState.value = 1
            }
        }
    }

    private fun onPermissionGranted(context: Context) {
        viewModelScope.launch {
            try {
                val location = withTimeoutOrNull(10000L) {
                    LocationProvider(context).fetchSingleLocation()
                }
                LogCat.e("latitude = ${location?.latitude} longitude = ${location?.longitude}")
                if (location != null) {
                    obtainLocationDataByLocation(location)
                } else {
                    _locationData.value = null
                    _locationState.value = 1
                }
            } catch (_: Exception) {
                _locationData.value = null
                _locationState.value = 1
            }
        }
    }

    private fun obtainLocationDataByLocation(location: Location) {
        ResultHandler.handleResultWithData(
            scope = viewModelScope,
            flow = weatherRepository.obtainLocationDataByLocation("${location.latitude},${location.longitude}")
                .asResult(),
            onData = { data ->
                _locationData.value = data
                _locationState.value = 1
                locationSuccess(data)
            },
            onError = { _, _ ->
                _locationData.value = null
                _locationState.value = 1
            }
        )
    }

    fun searchCity(searchKey: String, block: ((List<CityData>) -> Unit)? = null) {
        ResultHandler.handleResultWithData(
            scope = viewModelScope,
            flow = weatherRepository.searchCity(searchKey).asResult(),
            onData = { data ->
                if (block == null) {
                    if (data.isEmpty()) {
                        ToastUtils.show("无匹配城市")
                    }
                    _searchResult.value = data
                } else {
                    block.invoke(data)
                }
            },
            onError = { _, _ ->
                if (block == null) _searchResult.value = null
            }
        )
    }

    fun clearSearchResult() {
        viewModelScope.launch {
            _searchResult.value = null
        }
    }

    private fun locationSuccess(locationData: LocationData?) {
        if (locationData == null) return
        viewModelScope.launch {
            val locationCity = weatherDbRepository.getLocationCity()
            if (locationCity != null && TextUtils.isEmpty(locationCity.cityId)) {
                searchCity(locationData.addressComponent?.district ?: "") { searchResult ->
                    if (searchResult.isNotEmpty()) {
                        val province = locationData.addressComponent?.province ?: ""
                        val find = searchResult.find {
                            it.name == locationData.addressComponent?.district &&
                                    (province.contains(it.prov ?: "") || (it.prov ?: "").contains(
                                        province
                                    ))
                        }
                        if (find != null) {
                            val cityData = find.copy(
                                key = Constants.LOCATION_CITY_ID,
                                isLocationCity = true,
                                street = locationData.addressComponent?.street
                            )
                            addCity(cityData)
                        }
                    }
                }
            }
        }
    }

    fun gotoWeatherPreviewPage(cityData: CityData) {
        viewModelScope.launch {
            navigate(WeatherPreviewRoutes.WeatherPreview(cityData.cityId))
        }
    }

    fun addCity(cityData: CityData?) {
        if (cityData == null) {
            ToastUtils.show("数据异常，请稍后再试")
            return
        }
        viewModelScope.launch {
            val cities = cities.value
            val find = cities.find { it.cityId == cityData.cityId }
            if (find != null) {
                ToastUtils.show("该城市已经添加过了哦")
                return@launch
            }
            if (cities.size > Constants.MAX_CITY_LIST_LENGTH) {
                ToastUtils.show("城市数量已达上限，如果想要添加新的城市，请先删除已有的城市。")
                return@launch
            }
            val isLocationCity = cityData.isLocationCity
            if (isLocationCity) {
                weatherDbRepository.upsertCity(cityData)
            } else {
                val weatherData = emptySimpleWeatherData()
                weatherDbRepository.upsertCity(
                    cityData.copy(
                        key = cityData.cityId ?: "",
                        weatherData = weatherData
                    )
                )
            }
            AppRuntimeData.setCurrentCityData(cityData)
            val cityId = if (isLocationCity) Constants.LOCATION_CITY_ID else cityData.cityId
            if (!cityId.isNullOrEmpty()) {
                val currentCityIdList = MMKVUtils.getStringSet(Constants.CURRENT_CITY_ID_LIST)
                currentCityIdList.toMutableSet().apply {
                    add(cityId)
                    MMKVUtils.putStringSet(Constants.CURRENT_CITY_ID_LIST, this)
                }
            }
            navigateToOrBackTo(MainRoutes.Main)
        }
    }
}