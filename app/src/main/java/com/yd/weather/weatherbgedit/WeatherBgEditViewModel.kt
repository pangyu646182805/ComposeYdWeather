package com.yd.weather.weatherbgedit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.yd.weather.app.AppState
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.WeatherBgRoutes
import com.yd.weather.utils.DayPeriod
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

    private fun decode(json: String): List<Color> = json.takeIf { it.isNotEmpty() }
        ?.let { Json.decodeFromString<List<Int>>(it) }?.map { Color(it) } ?: emptyList()

    private val initialAfternoon = decode(route.colorsJson)
    private val initialNight = decode(route.nightColorsJson).ifEmpty { initialAfternoon }
    private val initialMorning = decode(route.morningColorsJson)
    private val initialDusk = decode(route.duskColorsJson)

    // 只用来在列表里找到被编辑的那条（按下午、夜间色匹配，见 AppState.addWeatherBg）
    private val editModel: WeatherBgModel? = if (isEdit && initialAfternoon.isNotEmpty()) {
        WeatherBgModel(
            supportEdit = true,
            isSelected = false,
            colors = initialAfternoon.map { it.value },
            nightColors = initialNight.map { it.value }
        )
    } else null

    // 列表页看的是哪个时段，就先打开哪个 tab
    private val _period = MutableStateFlow(
        DayPeriod.entries.firstOrNull { it.name == route.period } ?: DayPeriod.MORNING
    )
    val period: StateFlow<DayPeriod> = _period

    // 每个时段一套颜色（顶部、底部各一个）
    private val _colors = MutableStateFlow(
        mapOf(
            DayPeriod.MORNING to initialMorning.ifEmpty { initialAfternoon },
            DayPeriod.AFTERNOON to initialAfternoon,
            DayPeriod.DUSK to initialDusk.ifEmpty { initialAfternoon },
            DayPeriod.NIGHT to initialNight,
        )
    )
    val colors: StateFlow<Map<DayPeriod, List<Color>>> = _colors

    // HSV 单独存一份，不从颜色往回算：饱和度拉到 0 时色相会丢（参照 Flutter: _hsvColors）
    private val _hsvColors = MutableStateFlow(
        _colors.value.mapValues { (_, list) -> list.map { colorToHsv(it) } }
    )
    val hsvColors: StateFlow<Map<DayPeriod, List<FloatArray>>> = _hsvColors

    /** 上午、傍晚里还跟随下午的那几个：没单独改过就跟着下午走，下午改了它们也跟着变 */
    private val _followAfternoon = MutableStateFlow(
        buildSet {
            if (initialMorning.isEmpty()) add(DayPeriod.MORNING)
            if (initialDusk.isEmpty()) add(DayPeriod.DUSK)
        }
    )
    val followAfternoon: StateFlow<Set<DayPeriod>> = _followAfternoon

    private val _isStartSelected = MutableStateFlow(true)
    val isStartSelected: StateFlow<Boolean> = _isStartSelected

    /** 这个时段实际用的是哪套颜色：跟随下午的就是下午那套 */
    private fun shownPeriod(period: DayPeriod) =
        if (period in _followAfternoon.value) DayPeriod.AFTERNOON else period

    fun selectPeriod(period: DayPeriod) {
        _period.value = period
        _isStartSelected.value = true
    }

    fun selectStart(start: Boolean) {
        _isStartSelected.value = start
    }

    /** 当前的上午或傍晚改回跟随下午，单独设的颜色不要了 */
    fun followAfternoon() {
        val period = _period.value
        if (period == DayPeriod.MORNING || period == DayPeriod.DUSK) {
            _followAfternoon.value = _followAfternoon.value + period
        }
    }

    /** 获取当前选中颜色的 HSV */
    fun currentHsv(): FloatArray? {
        val index = if (_isStartSelected.value) 0 else 1
        return _hsvColors.value[shownPeriod(_period.value)]?.getOrNull(index)
    }

    /** 通过 HEX 字符串设置颜色（如 "FF0000"） */
    fun updateColorFromHex(hex: String) {
        val argb = try {
            android.graphics.Color.parseColor("#$hex")
        } catch (_: Exception) {
            return
        }
        val color = Color(argb)
        setCurrentColor(color, colorToHsv(color))
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
        setCurrentColor(hsvToColor(newHsv), newHsv)
    }

    /** 改当前 tab 里选中的那个颜色。上午、傍晚第一次改时先把下午那套抄过来，从此单独生效 */
    private fun setCurrentColor(color: Color, hsv: FloatArray) {
        val period = _period.value
        if (period in _followAfternoon.value) {
            _colors.value += period to _colors.value.getValue(DayPeriod.AFTERNOON)
            _hsvColors.value += period to _hsvColors.value.getValue(DayPeriod.AFTERNOON).map { it.copyOf() }
            _followAfternoon.value -= period
        }
        val index = if (_isStartSelected.value) 0 else 1
        val colorList = _colors.value.getValue(period).toMutableList()
        val hsvList = _hsvColors.value.getValue(period).toMutableList()
        if (index < colorList.size) colorList[index] = color
        if (index < hsvList.size) hsvList[index] = hsv
        _colors.value += period to colorList
        _hsvColors.value += period to hsvList
    }

    fun confirm() {
        val colors = _colors.value
        val follow = _followAfternoon.value
        // 跟随下午的不存，存下来的背景和以前一模一样
        val ownColors = { period: DayPeriod ->
            colors.getValue(period).takeUnless { period in follow }?.map { it.value }
        }
        val model = WeatherBgModel(
            supportEdit = true,
            isSelected = if (isEdit) editModel?.isSelected ?: false else false,
            colors = colors.getValue(DayPeriod.AFTERNOON).map { it.value },
            nightColors = colors.getValue(DayPeriod.NIGHT).map { it.value },
            morningColors = ownColors(DayPeriod.MORNING),
            duskColors = ownColors(DayPeriod.DUSK),
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
