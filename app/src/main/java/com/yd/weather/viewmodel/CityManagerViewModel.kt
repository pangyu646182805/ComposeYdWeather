package com.yd.weather.viewmodel

import androidx.lifecycle.viewModelScope
import com.yd.weather.app.AppState
import com.yd.weather.db.WeatherDbRepository
import com.yd.weather.db.model.CityData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.WeatherRepository
import com.yd.weather.routes.SelectCityRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        if (cityData == null) return
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

    fun toSelectCityPage() {
        navigate(SelectCityRoutes.SelectCity)
    }

    fun changeDeleteButtonEnable(enable: Boolean) {
        _deleteButtonEnable.value = enable
    }
}
