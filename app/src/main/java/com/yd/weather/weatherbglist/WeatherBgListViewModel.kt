package com.yd.weather.weatherbglist

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.yd.weather.app.AppState
import com.yd.weather.utils.DayPeriod
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.WeatherBgRoutes
import com.yd.weather.viewmodel.BaseViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    // 进来先看上午，右上角按上午、下午、傍晚、夜间循环切换
    private val _period = MutableStateFlow(DayPeriod.MORNING)
    val period: StateFlow<DayPeriod> = _period

    private val _isShowMenu = MutableStateFlow(false)
    val isShowMenu: StateFlow<Boolean> = _isShowMenu

    init {
        loadWeatherBgMap()
    }

    fun loadWeatherBgMap() {
        _weatherBgMap.value = _appState.getPublicWeatherBgMap()
    }

    fun nextPeriod() {
        _period.value = _period.value.next()
    }

    /** 某条背景在当前时段的颜色，内置背景的分时段颜色在色表里，见 AppState.weatherBgColors */
    fun bgColors(weatherType: String, model: WeatherBgModel, period: DayPeriod): List<Color> =
        _appState.weatherBgColors(weatherType, model, period)

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
        // 参照 Flutter: 先隐藏菜单，再删除
        _isShowMenu.value = false
        _appState.removeWeatherBg(weatherType, model)
        loadWeatherBgMap()
    }

    fun removeAllWeatherBg() {
        _appState.removeAllWeatherBg()
        _isShowMenu.value = false
        loadWeatherBgMap()
    }

    fun navigateToWeatherBgEdit(
        weatherType: String,
        model: WeatherBgModel?,
        isEdit: Boolean = false,
        isPreviewMode: Boolean = false
    ) {
        _isShowMenu.value = false
        val colorsOf = { p: DayPeriod -> _appState.weatherBgColors(weatherType, model, p) }
        val encode = { colors: List<Color> -> Json.encodeToString(colors.map { it.toArgb() }) }
        val afternoon = colorsOf(DayPeriod.AFTERNOON)
        // 上午、傍晚和下午一样就当作跟随下午，不一样才单独带过去（比如从内置的晴新建）
        val ownColors = { p: DayPeriod -> colorsOf(p).takeIf { it != afternoon }?.let(encode) ?: "" }
        navigate(
            WeatherBgRoutes.WeatherBgEdit(
                weatherType = weatherType,
                colorsJson = encode(afternoon),
                nightColorsJson = encode(colorsOf(DayPeriod.NIGHT)),
                morningColorsJson = ownColors(DayPeriod.MORNING),
                duskColorsJson = ownColors(DayPeriod.DUSK),
                period = _period.value.name,
                isEdit = isEdit,
                isPreviewMode = isPreviewMode
            )
        )
    }
}
