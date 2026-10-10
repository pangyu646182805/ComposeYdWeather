package com.yd.weather.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.luminance
import com.drake.logcat.LogCat
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.SunriseAndSunset
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.model.WeatherData
import com.yd.weather.model.WeatherHourData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.Commons
import com.yd.weather.utils.DayPeriod
import com.yd.weather.utils.MMKVUtils
import com.yd.weather.utils.toSafeColor
import com.yd.weather.utils.WeatherBgUtils
import com.yd.weather.utils.getToday
import com.yd.weather.utils.isLight
import com.yd.weather.utils.toDateString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AppState @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context
) {
    /**
     * 每种天气一条内置背景（supportEdit = false）。
     * 存储里只记白天色（下午那套）和夜间色，给背景列表页预览用；上午、傍晚这些分时段的颜色
     * 不进存储，用的时候直接从 [WeatherBgUtils] 取 —— 存储结构不变，装回旧版本也读得了。
     */
    val defaultWeatherBgMap: Map<String, List<WeatherBgModel>> = listOf(
        "CLEAR", "PARTLY_CLOUDY", "CLOUDY", "LIGHT_HAZE", "HEAVY_HAZE", "LIGHT_RAIN",
        "MODERATE_RAIN", "FOG", "LIGHT_SNOW", "DUST", "WIND",
    ).associateWith { type ->
        listOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = WeatherBgUtils.generateWeatherBg(type, DayPeriod.AFTERNOON).map { it.value },
                nightColors = WeatherBgUtils.generateWeatherBg(type, DayPeriod.NIGHT).map { it.value },
            )
        )
    }

    private val _clock = MutableStateFlow(System.currentTimeMillis())

    /**
     * 每分钟走一次的时钟，只在 app 位于前台时走（见 [runClock]）。
     * 天气背景按时段换色，读它是为了到点自动换；具体是几点仍以 getToday() 为准
     */
    val clock: StateFlow<Long> = _clock.asStateFlow()

    /**
     * 一直挂着直到协程被取消：先走一次（从后台回来时补上错过的切换），
     * 之后每个整分钟、用户改了系统时间或时区时各走一次
     */
    suspend fun runClock() {
        val tick = { _clock.value = System.currentTimeMillis() }
        tick()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = tick()
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        try {
            awaitCancellation()
        } finally {
            context.unregisterReceiver(receiver)
        }
    }

    private val _currentCityData = MutableStateFlow<CityData?>(null)
    val currentCityData: StateFlow<CityData?> = _currentCityData.asStateFlow()

    private val _weatherDataMap = MutableStateFlow<Map<String, WeatherData?>>(emptyMap())
    val weatherDataMap: StateFlow<Map<String, WeatherData?>> = _weatherDataMap.asStateFlow()

    private val _weatherBgMap = MutableStateFlow<Map<String, List<WeatherBgModel>>>(emptyMap())
    val weatherBgMap: StateFlow<Map<String, List<WeatherBgModel>>> = _weatherBgMap.asStateFlow()

    private val _currentWeatherCardSort =
        MutableStateFlow(Constants.DEFAULT_WEATHER_CARD_SORT.toTypedArray())
    val currentWeatherCardSort: StateFlow<Array<Int>> = _currentWeatherCardSort.asStateFlow()

    private val _currentWeatherObservesCardSort =
        MutableStateFlow(Constants.DEFAULT_WEATHER_OBSERVES_CARD_SORT.toTypedArray())
    val currentWeatherObservesCardSort: StateFlow<Array<Int>> =
        _currentWeatherObservesCardSort.asStateFlow()

    fun setCurrentCityData(cityData: CityData?) {
        _currentCityData.value = cityData
        if (cityData != null) {
            MMKVUtils.putString(
                Constants.CURRENT_CITY_ID,
                if (cityData.isLocationCity) Constants.LOCATION_CITY_ID else cityData.cityId
            )
        }
    }

    private fun getWeatherBgMap(): Map<String, List<WeatherBgModel>> {
        if (_weatherBgMap.value.isEmpty()) {
            val currentWeatherBgMap =
                MMKVUtils.getObject<Map<String, List<WeatherBgModel>>>(Constants.CURRENT_WEATHER_BG_MAP)
            val version = MMKVUtils.getInt(Constants.WEATHER_BG_DEFAULTS_VERSION_KEY, 1)
            val map = when {
                currentWeatherBgMap.isNullOrEmpty() -> defaultWeatherBgMap
                version < Constants.WEATHER_BG_DEFAULTS_VERSION -> migrateBuiltInBg(currentWeatherBgMap)
                else -> currentWeatherBgMap
            }
            if (map !== currentWeatherBgMap) {
                MMKVUtils.putObject(Constants.CURRENT_WEATHER_BG_MAP, map)
            }
            MMKVUtils.putInt(Constants.WEATHER_BG_DEFAULTS_VERSION_KEY, Constants.WEATHER_BG_DEFAULTS_VERSION)
            _weatherBgMap.value = map.toMap()
        }
        return this._weatherBgMap.value
    }

    /**
     * 默认背景第一次启动时就存进了 MMKV，之后一直读存下来的那份，代码里的默认色换了老用户也看不到。
     * 这里把每种天气的内置那条换成新的，用户自己加的原样保留，选中哪条也不变。
     *
     * 内置的认法：supportEdit 为 false。早先小雨的内置背景错设成了 true，
     * 所以小雨还要按当年的默认色认一遍。内置那条要是被删了（就是小雨那个 bug），补回来。
     */
    private fun migrateBuiltInBg(
        stored: Map<String, List<WeatherBgModel>>
    ): Map<String, List<WeatherBgModel>> {
        val legacyLightRain = listOf(0xFF5E738D, 0xFF8F9AAD).map { Color(it).value } to
                listOf(0xFF171A2A, 0xFF3C4354).map { Color(it).value }
        val isBuiltIn = { type: String, model: WeatherBgModel ->
            !model.supportEdit || (type == "LIGHT_RAIN" &&
                    model.colors to model.nightColors == legacyLightRain)
        }
        return defaultWeatherBgMap.mapValues { (type, defaults) ->
            val list = stored[type].orEmpty()
            val custom = list.filterNot { isBuiltIn(type, it) }
            val oldBuiltIn = list.firstOrNull { isBuiltIn(type, it) }
            val selected = oldBuiltIn?.isSelected ?: custom.none { it.isSelected }
            listOf(defaults.first().copy(isSelected = selected)) + custom
        }
    }

    fun getPublicWeatherBgMap(): Map<String, List<WeatherBgModel>> {
        return getWeatherBgMap()
    }

    fun setCurrentWeatherBg(weatherType: String, model: WeatherBgModel) {
        val map = getWeatherBgMap().toMutableMap()
        val list = map[weatherType]?.map {
            it.copy(isSelected = it == model)
        } ?: return
        map[weatherType] = list
        _weatherBgMap.value = map
        MMKVUtils.putObject(Constants.CURRENT_WEATHER_BG_MAP, map)
    }

    fun addWeatherBg(
        weatherType: String,
        model: WeatherBgModel,
        editModel: WeatherBgModel? = null
    ) {
        val map = getWeatherBgMap().toMutableMap()
        val list = map[weatherType]?.toMutableList() ?: return
        if (editModel != null) {
            // 编辑模式：替换旧的（用 colors/nightColors 匹配，忽略 isSelected 和精度差异）
            val index = list.indexOfFirst {
                it.colors == editModel.colors && it.nightColors == editModel.nightColors
            }
            if (index >= 0) {
                list[index] = model.copy(isSelected = list[index].isSelected)
            }
        } else {
            // 新增模式
            list.add(model)
        }
        map[weatherType] = list
        _weatherBgMap.value = map
        MMKVUtils.putObject(Constants.CURRENT_WEATHER_BG_MAP, map)
    }

    fun removeWeatherBg(weatherType: String, model: WeatherBgModel) {
        val map = getWeatherBgMap().toMutableMap()
        val list = map[weatherType]?.toMutableList() ?: return
        val index = list.indexOfFirst { it == model }
        if (index >= 0) list.removeAt(index)
        if (model.isSelected && list.isNotEmpty()) {
            list[0] = list[0].copy(isSelected = true)
        }
        map[weatherType] = list
        _weatherBgMap.value = map
        MMKVUtils.putObject(Constants.CURRENT_WEATHER_BG_MAP, map)
    }

    fun removeAllWeatherBg() {
        _weatherBgMap.value = defaultWeatherBgMap.toMap()
        MMKVUtils.putObject(Constants.CURRENT_WEATHER_BG_MAP, defaultWeatherBgMap)
        MMKVUtils.putInt(Constants.WEATHER_BG_DEFAULTS_VERSION_KEY, Constants.WEATHER_BG_DEFAULTS_VERSION)
    }

    private fun todayWeatherDetail(weatherData: WeatherData?) =
        weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }

    /** 这份天气数据所在城市当前的时段，日出日落优先用当天预报里的 */
    fun dayPeriodOf(
        weatherData: WeatherData?,
        cacheSunrise: String? = null,
        cacheSunset: String? = null
    ): DayPeriod {
        val today = todayWeatherDetail(weatherData)
        return Commons.dayPeriod(
            getToday(),
            today?.sunrise ?: cacheSunrise,
            today?.sunset ?: cacheSunset
        )
    }

    fun generateWeatherBg(
        weatherData: WeatherData?,
        cacheWeatherType: String? = null,
        cacheSunrise: String? = null,
        cacheSunset: String? = null,
        period: DayPeriod = dayPeriodOf(weatherData, cacheSunrise, cacheSunset)
    ): List<Color> {
        var weatherType = cacheWeatherType ?: (weatherData?.observe?.thirdType ?: "")
        if (weatherType.isEmpty()) {
            weatherType = todayWeatherDetail(weatherData)?.thirdType ?: ""
        }
        return generateWeatherBg(weatherType, period, true)
    }

    fun generateWeatherBg(
        type: String,
        period: DayPeriod,
        getWeatherBgMap: Boolean = false
    ): List<Color> {
        if (getWeatherBgMap) {
            val weatherBgMap = getWeatherBgMap()
            val find = weatherBgMap[WeatherBgUtils.fixedWeatherType(type)]?.find { it.isSelected }
            // 用户自己加的背景只有白天、夜间两套；内置的走下面，分时段的颜色在代码里
            if (find != null && find.supportEdit) {
                val colors = if (period == DayPeriod.NIGHT) find.nightColors else find.colors
                return colors.map { it.toSafeColor() }
            }
        }
        return WeatherBgUtils.generateWeatherBg(type, period)
    }

    fun isWeatherHeaderDark(weatherBg: List<Color>?): Boolean {
        if (weatherBg.isNullOrEmpty()) return false
        return !weatherBg.first().isLight()
    }

    fun isDark(weatherBg: List<Color>?): Boolean {
        if (weatherBg.isNullOrEmpty()) return false
        return !weatherBg[1].isLight()
    }

    fun calPanelOpacity(weatherBg: List<Color>?): Float {
        if (weatherBg.isNullOrEmpty()) return 0.1f
        val color = weatherBg[1]
        val darkness = 1 - color.luminance()
        var panelOpacity = abs(0.3 - darkness).toFloat()
        if (panelOpacity < 0.1) {
            panelOpacity = 0.1f
        } else if (panelOpacity > 0.3) {
            panelOpacity = 0.3f
        }
        LogCat.e("darkness = $darkness panelOpacity = $panelOpacity")
        return panelOpacity
    }

    fun saveWeatherData(key: String, weatherData: WeatherData?) {
        if (weatherData != null) {
            _weatherDataMap.value = _weatherDataMap.value.plus(key to weatherData)
            MMKVUtils.putObject(key, weatherData)
        } else {
            _weatherDataMap.value -= key
            MMKVUtils.putObject(key, null)
        }
    }

    fun getWeatherData(key: String): WeatherData? {
        val weatherData = _weatherDataMap.value[key]
        if (weatherData != null) return weatherData
        return MMKVUtils.getObject(key)
    }

    fun getItemTypeObserves(
        currentWeatherObservesCardSort: Array<Int>,
        itemType: Int,
        weatherData: WeatherData?
    ): Array<Int>? {
        if (weatherData != null && itemType == Constants.ITEM_TYPE_OBSERVE) {
            val currentWeatherDetailData =
                weatherData.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
            val observe = weatherData.observe
            // UV 数据优先取实况，若无则降级取今日预报
            val uvIndex = if ((observe?.uvIndex ?: 0) > 0) observe?.uvIndex
                ?: 0 else currentWeatherDetailData?.uvIndex ?: 0
            val uvIndexMax = if ((observe?.uvIndexMax ?: 0) > 0) observe?.uvIndexMax
                ?: 0 else currentWeatherDetailData?.uvIndexMax ?: 0
            val uvLevel =
                if (!observe?.uvLevel.isNullOrEmpty()) observe.uvLevel else currentWeatherDetailData?.uvLevel

            return currentWeatherObservesCardSort.filter { subItemType ->
                val removeUvPanel = subItemType == Constants.ITEM_TYPE_OBSERVE_UV &&
                        (uvIndex <= 0 || uvIndexMax <= 0 || uvLevel.isNullOrEmpty())
                val removeShiDuPanel = subItemType == Constants.ITEM_TYPE_OBSERVE_SHI_DU &&
                        observe?.shiDu.isNullOrEmpty()
                val removeTiGanPanel = subItemType == Constants.ITEM_TYPE_OBSERVE_TI_GAN &&
                        observe?.tiGan.isNullOrEmpty()
                val removeWdPanel = subItemType == Constants.ITEM_TYPE_OBSERVE_WD &&
                        (observe?.wd.isNullOrEmpty() || observe.wp.isNullOrEmpty())
                val removeSunriseSunsetPanel =
                    subItemType == Constants.ITEM_TYPE_OBSERVE_SUNRISE_SUNSET &&
                            (currentWeatherDetailData?.sunrise.isNullOrEmpty() || currentWeatherDetailData.sunset.isNullOrEmpty())
                val removePressurePanel = subItemType == Constants.ITEM_TYPE_OBSERVE_PRESSURE &&
                        observe?.pressure.isNullOrEmpty()
                val removeVisibilityPanel = subItemType == Constants.ITEM_TYPE_OBSERVE_VISIBILITY &&
                        (observe?.visibility.isNullOrEmpty() || currentWeatherDetailData?.visibility.isNullOrEmpty())
                val removeForecast40Panel = subItemType == Constants.ITEM_TYPE_OBSERVE_FORECAST40 &&
                        weatherData.forecast40.isNullOrEmpty()
                !removeUvPanel && !removeShiDuPanel && !removeTiGanPanel && !removeWdPanel &&
                        !removeSunriseSunsetPanel && !removePressurePanel && !removeVisibilityPanel && !removeForecast40Panel
            }.toTypedArray()
        }
        return null
    }

    fun setCurrentWeatherCardSort(currentWeatherCardSort: Array<Int>) {
        _currentWeatherCardSort.value = currentWeatherCardSort
        MMKVUtils.putObject(Constants.CURRENT_WEATHER_CARD_SORT, currentWeatherCardSort)
    }

    fun setCurrentWeatherObservesCardSort(currentWeatherObservesCardSort: Array<Int>) {
        _currentWeatherObservesCardSort.value = currentWeatherObservesCardSort
        MMKVUtils.putObject(
            Constants.CURRENT_WEATHER_OBSERVES_CARD_SORT,
            currentWeatherObservesCardSort
        )
    }

    fun generateWeatherItems(
        itemTypeObserves: Array<Int>?,
        weatherData: WeatherData?
    ): List<WeatherItemData> {
        val newWeatherData = generateWeatherHourFc(weatherData)
        val currentWeatherCardSort = _currentWeatherCardSort.value
        val weatherItems = currentWeatherCardSort.map { itemType ->
            WeatherItemData(itemType, newWeatherData)
        }
        return weatherItems.filter { weatherItem ->
            val itemType = weatherItem.itemType
            val removeAlarmsPanel = itemType == Constants.ITEM_TYPE_ALARMS && newWeatherData?.alarms.isNullOrEmpty()
            val removeAirQualityPanel = itemType == Constants.ITEM_TYPE_AIR_QUALITY && newWeatherData?.evn == null
            val removeHourPanel = itemType == Constants.ITEM_TYPE_HOUR_WEATHER && newWeatherData?.hourFc.isNullOrEmpty()
            val removeDailyPanel = itemType == Constants.ITEM_TYPE_DAILY_WEATHER && newWeatherData?.forecast15.isNullOrEmpty()
            val removeObservePanel = itemType == Constants.ITEM_TYPE_OBSERVE && itemTypeObserves.isNullOrEmpty()
            val removeLifeIndexPanel = itemType == Constants.ITEM_TYPE_LIFE_INDEX && newWeatherData?.indexes.isNullOrEmpty()
            !removeAlarmsPanel && !removeAirQualityPanel && !removeHourPanel &&
                    !removeDailyPanel && !removeObservePanel && !removeLifeIndexPanel
        }
    }

    private fun generateWeatherHourFc(weatherData: WeatherData?): WeatherData? {
        val hourFc = weatherData?.hourFc ?: return weatherData
        val hourFcFilter =
            hourFc.filter { it.sunriseAndSunset?.sunrise.isNullOrEmpty() && it.sunriseAndSunset?.sunset.isNullOrEmpty() }
                .toMutableList()
        val currentWeatherDetailData =
            weatherData.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
        val sunriseIndex = hourFcFilter.indexOfFirst {
            Commons.isSunriseOrSunset(
                currentWeatherDetailData?.sunrise, it.time
            )
        }
        if (sunriseIndex >= 0) {
            hourFcFilter.add(
                sunriseIndex + 1,
                WeatherHourData(
                    time = currentWeatherDetailData?.sunrise,
                    sunriseAndSunset = SunriseAndSunset(sunrise = currentWeatherDetailData?.sunrise)
                )
            )
        }
        val sunsetIndex = hourFcFilter.indexOfFirst {
            Commons.isSunriseOrSunset(
                currentWeatherDetailData?.sunset, it.time
            )
        }
        if (sunsetIndex >= 0) {
            hourFcFilter.add(
                sunsetIndex + 1,
                WeatherHourData(
                    time = currentWeatherDetailData?.sunset,
                    sunriseAndSunset = SunriseAndSunset(sunset = currentWeatherDetailData?.sunset)
                )
            )
        }
        return weatherData.copy(hourFc = hourFcFilter)
    }
}