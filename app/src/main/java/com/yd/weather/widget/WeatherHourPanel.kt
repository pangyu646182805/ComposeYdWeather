package com.yd.weather.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.model.WeatherHourData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.Commons
import com.yd.weather.utils.WeatherIconUtils
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.getFormatDate
import com.yd.weather.utils.getToday
import com.yd.weather.utils.toDateString

@Composable
fun WeatherHourPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    enable: Boolean = true
) {
    val weatherData = item.weatherData
    val hourFc = weatherData?.hourFc
    val currentWeatherDetailData =
        weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.WEATHER_HOUR_PANEL_HEIGHT,
        stickyTitle = "每小时天气预报"
    ) { offsetPx, _, _ ->
        AppColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(WeatherPanelClip(offsetPx))
        ) {
            VerticalSpace(height = Constants.ITEM_STICKY_HEIGHT.dp)
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colorResource(R.color.color_white).copy(alpha = 0.2f)
            )
            if (!hourFc.isNullOrEmpty()) {
                val displayHourFc = if (enable) hourFc else hourFc.take(7)
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    userScrollEnabled = enable
                ) {
                    items(displayHourFc) { item ->
                        HourItem(item, currentWeatherDetailData)
                    }
                }
            }
        }
    }
}

@Composable
internal fun HourItem(
    item: WeatherHourData,
    currentWeatherDetailData: WeatherDetailData?
) {
    val isSunrise = isSunrise(item)
    val isSunset = isSunset(item)
    WrapColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalSpace(height = 12.dp)
        AppText(
            text = if (isSunrise) item.sunriseAndSunset?.sunrise
                ?: "" else if (isSunset) item.sunriseAndSunset?.sunset
                ?: "" else Commons.getWeatherHourTime(
                item.time,
                currentWeatherDetailData?.sunrise,
                currentWeatherDetailData?.sunset
            ),
            fontSize = 13.sp,
            color = colorResource(R.color.color_white)
        )
        VerticalSpace(height = 12.dp)
        CommonIcon(
            resId = if (isSunrise) R.mipmap.ic_sunrise_icon else if (isSunset) R.mipmap.ic_sunset_icon else WeatherIconUtils.getWeatherIconByType(
                item.type,
                item.thirdType ?: "",
                Commons.isNight(
                    getFormatDate(item.time ?: ""),
                    currentWeatherDetailData?.sunrise,
                    currentWeatherDetailData?.sunset
                )
            ),
            size = 24.dp,
            tint = Color.Unspecified
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = if (isSunrise) "日出" else if (isSunset) "日落" else Commons.getTemp(item.wthr),
            fontSize = 13.sp,
            color = colorResource(R.color.color_white)
        )
    }
}

internal fun isSunrise(item: WeatherHourData): Boolean {
    val sunriseAndSunset = item.sunriseAndSunset
    if (sunriseAndSunset != null) {
        return !sunriseAndSunset.sunrise.isNullOrEmpty()
    }
    return false
}

internal fun isSunset(item: WeatherHourData): Boolean {
    val sunriseAndSunset = item.sunriseAndSunset
    if (sunriseAndSunset != null) {
        return !sunriseAndSunset.sunset.isNullOrEmpty()
    }
    return false
}
