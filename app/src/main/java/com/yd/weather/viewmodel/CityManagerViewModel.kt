package com.yd.weather.viewmodel

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

    var listOffsetY = 0f

    var fullyVisibleIndices = emptyList<Int>()

    fun appState(): AppState = _appState

    fun closeEditMode() {
        _isEditMode.value = false
        clearSelected()
        // 退出编辑模式时拖拽未必已经结束（长按不松手、另一只手点 x 就是这种情况），
        // 手势被掐断时 onDragStopped 不一定会来，删除按钮的禁用状态在这里一并复位，
        // 否则下次进编辑模式删除键是灰的
        _deleteButtonEnable.value = true
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

    /**
     * 一镜到底收回时让城市卡片整批回来。
     *
     * 由天气页在卡片落地之前调用（那时卡片还比列表项大一圈，正好盖住它），
     * 所以不再错峰逐张淡入：小米天气也是一次性出来的，错峰只会让收回拖长半秒多。
     */
    fun showCityList() {
        _itemAlpha.value = 1f
    }

    /** 一镜到底展开的第一帧把其余城市卡片藏掉，放大的卡片在干净的底上长大 */
    fun hideCityList() {
        _itemAlpha.value = 0f
    }
}
