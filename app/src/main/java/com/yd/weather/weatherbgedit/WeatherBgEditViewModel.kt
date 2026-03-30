package com.yd.weather.weatherbgedit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.yd.weather.app.AppState
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.WeatherBgRoutes
import com.yd.weather.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class WeatherBgEditViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(navigator, appState) {
    private val route = savedStateHandle.toRoute<WeatherBgRoutes.WeatherBgEdit>()
    fun appState(): AppState = super.appState

    val weatherType: String = route.weatherType
    val isEdit: Boolean = route.isEdit
    val isPreviewMode: Boolean = route.isPreviewMode

    private val initialArgbColors: List<Int> = route.colorsJson.takeIf { it.isNotEmpty() }
        ?.let { Json.decodeFromString<List<Int>>(it) } ?: emptyList()
    private val initialArgbNightColors: List<Int> = route.nightColorsJson.takeIf { it.isNotEmpty() }
        ?.let { Json.decodeFromString<List<Int>>(it) } ?: emptyList()

    private val editModel: WeatherBgModel? = if (isEdit && initialArgbColors.isNotEmpty()) {
        WeatherBgModel(
            supportEdit = true,
            isSelected = false,
            colors = initialArgbColors.map { Color(it).value },
            nightColors = initialArgbNightColors.ifEmpty { initialArgbColors }.map { Color(it).value }
        )
    } else null

    private val _isNight = MutableStateFlow(false)
    val isNight: StateFlow<Boolean> = _isNight

    // 颜色列表（RGB）
    private val _colors = MutableStateFlow(initialArgbColors.map { Color(it) }.toMutableList())
    val colors: StateFlow<List<Color>> = _colors

    private val _nightColors = MutableStateFlow(
        initialArgbNightColors.ifEmpty { initialArgbColors }.map { Color(it) }.toMutableList()
    )
    val nightColors: StateFlow<List<Color>> = _nightColors

    // HSV 列表（参照 Flutter: _hsvColors / _hsvNightColors）
    private val _hsvColors = MutableStateFlow(
        initialArgbColors.map { colorToHsv(Color(it)) }.toMutableList()
    )
    val hsvColors: StateFlow<List<FloatArray>> = _hsvColors

    private val _hsvNightColors = MutableStateFlow(
        initialArgbNightColors.ifEmpty { initialArgbColors }.map { colorToHsv(Color(it)) }.toMutableList()
    )
    val hsvNightColors: StateFlow<List<FloatArray>> = _hsvNightColors

    private val _isStartSelected = MutableStateFlow(true)
    val isStartSelected: StateFlow<Boolean> = _isStartSelected

    fun toggleNight(night: Boolean) {
        _isNight.value = night
        _isStartSelected.value = true
    }

    fun selectStart(start: Boolean) {
        _isStartSelected.value = start
    }

    /** 获取当前选中颜色的 HSV */
    fun currentHsv(): FloatArray? {
        val index = if (_isStartSelected.value) 0 else 1
        val list = if (_isNight.value) _hsvNightColors.value else _hsvColors.value
        return list.getOrNull(index)
    }

    /** 通过 HEX 字符串设置颜色（如 "FF0000"） */
    fun updateColorFromHex(hex: String) {
        val argb = try {
            android.graphics.Color.parseColor("#$hex")
        } catch (_: Exception) {
            return
        }
        val color = Color(argb)
        val newHsv = colorToHsv(color)
        val index = if (_isStartSelected.value) 0 else 1
        if (_isNight.value) {
            val hsvList = _hsvNightColors.value.toMutableList()
            val colorList = _nightColors.value.toMutableList()
            if (index < hsvList.size) hsvList[index] = newHsv
            if (index < colorList.size) colorList[index] = color
            _hsvNightColors.value = hsvList
            _nightColors.value = colorList
        } else {
            val hsvList = _hsvColors.value.toMutableList()
            val colorList = _colors.value.toMutableList()
            if (index < hsvList.size) hsvList[index] = newHsv
            if (index < colorList.size) colorList[index] = color
            _hsvColors.value = hsvList
            _colors.value = colorList
        }
    }

    /** 更新色相（只改 H，S/V 不变） */
    fun updateHue(hue: Float) {
        val hsv = currentHsv() ?: return
        updateHsvComponent(floatArrayOf(hue.coerceIn(0f, 359.9f), hsv[1], hsv[2]))
    }

    /** 更新饱和度（只改 S，H/V 不变） */
    fun updateSaturation(saturation: Float) {
        val hsv = currentHsv() ?: return
        updateHsvComponent(floatArrayOf(hsv[0], saturation.coerceIn(0f, 1f), hsv[2]))
    }

    /** 更新亮度（只改 V，H/S 不变） */
    fun updateValue(value: Float) {
        val hsv = currentHsv() ?: return
        updateHsvComponent(floatArrayOf(hsv[0], hsv[1], value.coerceIn(0f, 1f)))
    }

    private fun updateHsvComponent(newHsv: FloatArray) {
        val index = if (_isStartSelected.value) 0 else 1
        val color = hsvToColor(newHsv)

        if (_isNight.value) {
            val hsvList = _hsvNightColors.value.toMutableList()
            val colorList = _nightColors.value.toMutableList()
            if (index < hsvList.size) hsvList[index] = newHsv
            if (index < colorList.size) colorList[index] = color
            _hsvNightColors.value = hsvList
            _nightColors.value = colorList
        } else {
            val hsvList = _hsvColors.value.toMutableList()
            val colorList = _colors.value.toMutableList()
            if (index < hsvList.size) hsvList[index] = newHsv
            if (index < colorList.size) colorList[index] = color
            _hsvColors.value = hsvList
            _colors.value = colorList
        }
    }

    fun confirm() {
        val model = WeatherBgModel(
            supportEdit = true,
            isSelected = if (isEdit) editModel?.isSelected ?: false else false,
            colors = _colors.value.map { it.value },
            nightColors = _nightColors.value.map { it.value }
        )
        appState.addWeatherBg(weatherType, model, editModel)
        navigateBack()
    }

    companion object {
        fun colorToHsv(color: Color): FloatArray {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(
                    (color.alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                ), hsv
            )
            return hsv
        }

        fun hsvToColor(hsv: FloatArray): Color {
            val argb = android.graphics.Color.HSVToColor(hsv)
            return Color(argb)
        }
    }
}
