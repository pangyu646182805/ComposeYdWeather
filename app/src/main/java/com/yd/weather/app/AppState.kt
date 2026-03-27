package com.yd.weather.app

import android.content.Context
import androidx.compose.ui.graphics.Color
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
import com.yd.weather.utils.MMKVUtils
import com.yd.weather.utils.WeatherBgUtils
import com.yd.weather.utils.getToday
import com.yd.weather.utils.isLight
import com.yd.weather.utils.toDateString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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
    val defaultWeatherBgMap = mapOf<String, List<WeatherBgModel>>(
        "CLEAR" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("CLEAR", false).map { it.value },
                nightColors = generateWeatherBg("CLEAR", true).map { it.value },
            )
        ),
        "PARTLY_CLOUDY" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("PARTLY_CLOUDY", false).map { it.value },
                nightColors = generateWeatherBg("PARTLY_CLOUDY", true)
                    .map { it.value },
            )
        ),
        "CLOUDY" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("CLOUDY", false).map { it.value },
                nightColors = generateWeatherBg("CLOUDY", true).map { it.value },
            )
        ),
        "LIGHT_HAZE" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("LIGHT_HAZE", false).map { it.value },
                nightColors = generateWeatherBg("LIGHT_HAZE", true).map { it.value },
            )
        ),
        "HEAVY_HAZE" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("HEAVY_HAZE", false).map { it.value },
                nightColors = generateWeatherBg("HEAVY_HAZE", true).map { it.value },
            )
        ),
        "LIGHT_RAIN" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = true,
                colors = generateWeatherBg("LIGHT_RAIN", false).map { it.value },
                nightColors = generateWeatherBg("LIGHT_RAIN", true).map { it.value },
            )
        ),
        "MODERATE_RAIN" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("MODERATE_RAIN", false).map { it.value },
                nightColors = generateWeatherBg("MODERATE_RAIN", true)
                    .map { it.value },
            )
        ),
        "FOG" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("FOG", false).map { it.value },
                nightColors = generateWeatherBg("FOG", true).map { it.value },
            )
        ),
        "LIGHT_SNOW" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("LIGHT_SNOW", false).map { it.value },
                nightColors = generateWeatherBg("LIGHT_SNOW", true).map { it.value },
            )
        ),
        "DUST" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("DUST", false).map { it.value },
                nightColors = generateWeatherBg("DUST", true).map { it.value },
            )
        ),
        "WIND" to arrayListOf(
            WeatherBgModel(
                isSelected = true,
                supportEdit = false,
                colors = generateWeatherBg("WIND", false).map { it.value },
                nightColors = generateWeatherBg("WIND", true).map { it.value },
            )
        ),
    )

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
            if (currentWeatherBgMap.isNullOrEmpty()) {
                MMKVUtils.putObject(
                    Constants.CURRENT_WEATHER_BG_MAP,
                    defaultWeatherBgMap
                )
                _weatherBgMap.value = defaultWeatherBgMap.toMap()
            } else {
                _weatherBgMap.value = currentWeatherBgMap.toMap()
            }
        }
        return this._weatherBgMap.value
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
    }

    fun generateWeatherBg(
        weatherData: WeatherData?,
        cacheWeatherType: String? = null,
        cacheSunrise: String? = null,
        cacheSunset: String? = null
    ): List<Color> {
        var weatherType = cacheWeatherType ?: (weatherData?.observe?.thirdType ?: "")
        val currentWeatherDetailData =
            weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
        if (weatherType.isEmpty()) {
            weatherType = currentWeatherDetailData?.thirdType ?: ""
        }
        return generateWeatherBg(
            weatherType,
            Commons.isNight(
                getToday(),
                currentWeatherDetailData?.sunrise ?: cacheSunrise,
                currentWeatherDetailData?.sunset ?: cacheSunset
            ), true
        )
    }

    fun generateWeatherBg(
        type: String,
        isDark: Boolean,
        getWeatherBgMap: Boolean = false
    ): List<Color> {
        if (getWeatherBgMap) {
            val weatherBgMap = getWeatherBgMap()
            val find = weatherBgMap[WeatherBgUtils.fixedWeatherType(type)]?.find { it.isSelected }
            if (find != null) {
                return if (isDark) find.nightColors.map { Color(it) } else find.colors.map {
                    Color(it)
                }
            }
        }
        return WeatherBgUtils.generateWeatherBg(context, type, isDark).map { Color(it) }
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