package com.yd.weather.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import com.yd.weather.app.AppState
import com.yd.weather.config.Constants
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.CityData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.WeatherRepository
import com.yd.weather.routes.SelectCityRoutes
import com.yd.weather.utils.MMKVUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CityManagerViewModel @Inject constructor(
    navigator: AppNavigator,
    private val _appState: AppState,
    private val weatherRepository: WeatherRepository,
    private val weatherDbRepository: WeatherDbRepository,
) : BaseViewModel(navigator, _appState) {
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val _selectedList = MutableStateFlow<List<CityData>>(emptyList())
    val selectedList: StateFlow<List<CityData>> = _selectedList

    private val _deleteButtonEnable = MutableStateFlow(true)
    val deleteButtonEnable: StateFlow<Boolean> = _deleteButtonEnable

    private val _itemAlpha = MutableStateFlow(0f)
    val itemAlpha: StateFlow<Float> = _itemAlpha

    var startIndex = 0

    var endIndex = 0

    var listOffsetY = 0f

    var listHeight = 0

    var fullyVisibleIndices = emptyList<Int>()

    fun appState(): AppState = _appState

    fun closeEditMode() {
        _isEditMode.value = false
        clearSelected()
    }

    fun toEditMode(citySize: Int, cityData: CityData?) {
        if (!_isEditMode.value && cityData != null) {
            val isLocationCity = cityData.isLocationCity
            if (citySize <= 1 && isLocationCity) return
            _isEditMode.value = true
            if (!isLocationCity) {
                selected(cityData)
            }
        }
    }

    fun isSelectedAll(addedCityData: List<CityData>?): Boolean {
        if (addedCityData.isNullOrEmpty()) return false
        val find = addedCityData.find { it.isLocationCity }
        return if (find == null)
            _selectedList.value.size == addedCityData.size
        else
            _selectedList.value.size == addedCityData.size - 1
    }

    fun selected(cityData: CityData?) {
        if (cityData == null || cityData.isLocationCity) return
        val findIndex = _selectedList.value.indexOfFirst { it.cityId == cityData.cityId }
        if (findIndex >= 0) {
            _selectedList.value.toMutableList().apply {
                removeAt(findIndex)
                _selectedList.value = this
            }
        } else {
            _selectedList.value += cityData
        }
    }

    fun selectedAll(addedCityData: List<CityData>?) {
        if (addedCityData.isNullOrEmpty()) return
        clearSelected()
        _selectedList.value.toMutableList().apply {
            addAll(addedCityData.filter { !it.isLocationCity })
            _selectedList.value = this
        }
    }

    fun isSelected(cityData: CityData?): Boolean {
        if (!_isEditMode.value) return false
        if (cityData == null) return false
        return _selectedList.value.find { it.cityId == cityData.cityId } != null
    }

    fun hasSelected(): Boolean {
        return _selectedList.value.isNotEmpty()
    }

    fun clearSelected() {
        _selectedList.value = emptyList()
    }

    fun toSelectCityPage(replace: Boolean = false) {
        if (replace) {
            navigateToOrBackTo(SelectCityRoutes.SelectCity)
        } else {
            navigate(SelectCityRoutes.SelectCity)
        }
    }

    fun changeDeleteButtonEnable(enable: Boolean) {
        _deleteButtonEnable.value = enable
    }

    fun refreshCurrentCityIdList(removeItems: List<CityData>) {
        val currentCityIdList =
            MMKVUtils.getStringSet(Constants.CURRENT_CITY_ID_LIST).toMutableSet()
        removeItems.forEach { removeItem ->
            currentCityIdList.removeIf { it == removeItem.cityId }
            appState.saveWeatherData(removeItem.key, null)
        }
        MMKVUtils.putStringSet(Constants.CURRENT_CITY_ID_LIST, currentCityIdList)
    }

    fun afterRemove(resetCurrentCityData: Boolean, addedCityData: List<CityData>?) {
        if (addedCityData.isNullOrEmpty()) {
            MMKVUtils.putString(Constants.CURRENT_CITY_ID, "")
            toSelectCityPage(replace = true)
        } else {
            if (resetCurrentCityData) {
                appState.setCurrentCityData(addedCityData.firstOrNull())
            }
        }
    }

    fun showCityList(addedCityData: List<CityData>?, cityManagerScrollState: LazyListState) {
        if (addedCityData.isNullOrEmpty()) return
        val currentCityData = appState.currentCityData.value ?: return
        val index = addedCityData.indexOfFirst { it.cityId == currentCityData.cityId }
        if (index >= 0) {
            val visibleItemsInfo = cityManagerScrollState.layoutInfo.visibleItemsInfo
            val visibleIndices = visibleItemsInfo.map { it.index - 1 }
            if (visibleIndices.isNotEmpty()) {
                val itemInfo = visibleItemsInfo.firstOrNull { it.index == index + 1 }
                val itemOffsetY = itemInfo?.offset ?: 0
                if (itemOffsetY > listHeight * 0.5f) {
                    startIndex = visibleIndices.last()
                    endIndex = visibleIndices.first()
                } else {
                    startIndex = visibleIndices.first()
                    endIndex = visibleIndices.last()
                }
                _itemAlpha.value = 1f
            }
        }
    }

    fun hideCityList() {
        startIndex = 0
        endIndex = 0
        _itemAlpha.value = 0f
    }
}
