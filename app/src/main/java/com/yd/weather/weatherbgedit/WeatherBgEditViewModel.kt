package com.yd.weather.weatherbgedit

import androidx.compose.ui.graphics.Color
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
) : BaseViewModel(navigator, appState) {
    fun appState(): AppState = super.appState

    var weatherType: String = ""
        private set
    var isEdit: Boolean = false
        private set
    var isPreviewMode: Boolean = false
        private set

    private var initialArgbColors: List<Int> = emptyList()
    private var initialArgbNightColors: List<Int> = emptyList()
    private var editModel: WeatherBgModel? = null
    private val _isNight = MutableStateFlow(false)
    val isNight: StateFlow<Boolean> = _isNight

    private val _colors = MutableStateFlow<MutableList<Color>>(mutableListOf())
    val colors: StateFlow<List<Color>> = _colors

    private val _nightColors = MutableStateFlow<MutableList<Color>>(mutableListOf())
    val nightColors: StateFlow<List<Color>> = _nightColors

    private val _hsvColors = MutableStateFlow<MutableList<FloatArray>>(mutableListOf())
    val hsvColors: StateFlow<List<FloatArray>> = _hsvColors

    private val _hsvNightColors = MutableStateFlow<MutableList<FloatArray>>(mutableListOf())
    val hsvNightColors: StateFlow<List<FloatArray>> = _hsvNightColors

    private val _isStartSelected = MutableStateFlow(true)
    val isStartSelected: StateFlow<Boolean> = _isStartSelected

    /**
     * 由 Composable 调用，传入路由参数并触发初始化
     */
    fun initialize(route: WeatherBgRoutes.WeatherBgEdit) {
        if (weatherType.isNotEmpty()) return

        weatherType = route.weatherType
        isEdit = route.isEdit
        isPreviewMode = route.isPreviewMode

        initialArgbColors = route.colorsJson.takeIf { it.isNotEmpty() }
            ?.let { Json.decodeFromString<List<Int>>(it) } ?: emptyList()
        initialArgbNightColors = route.nightColorsJson.takeIf { it.isNotEmpty() }
            ?.let { Json.decodeFromString<List<Int>>(it) } ?: emptyList()

        editModel = if (isEdit && initialArgbColors.isNotEmpty()) {
            WeatherBgModel(
                supportEdit = true,
                isSelected = false,
                colors = initialArgbColors.map { Color(it).value },
                nightColors = initialArgbNightColors.ifEmpty { initialArgbColors }
                    .map { Color(it).value }
            )
        } else null

        _colors.value = initialArgbColors.map { Color(it) }.toMutableList()
        _nightColors.value =
            initialArgbNightColors.ifEmpty { initialArgbColors }.map { Color(it) }.toMutableList()
        _hsvColors.value = initialArgbColors.map { colorToHsv(Color(it)) }.toMutableList()
        _hsvNightColors.value =
            initialArgbNightColors.ifEmpty { initialArgbColors }.map { colorToHsv(Color(it)) }
                .toMutableList()
    }

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
