package com.yd.weather.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.Commons
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.getToday
import com.yd.weather.utils.toDateString

@Composable
fun WeatherObserveSunriseSunsetPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData
    val currentWeatherDetailData =
        weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }

    val sunrise = currentWeatherDetailData?.sunrise ?: ""
    val sunset = currentWeatherDetailData?.sunset ?: ""

    val isNight = Commons.isNight(getToday(), sunrise, sunset)
    val sunriseSunsetDesc = if (isNight) "日出" else "日落"
    val sunriseSunsetTime = if (isNight) sunrise else sunset

    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "日出日落"
    ) { offsetPx, _, _ ->
        AppRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(WeatherPanelClip(offsetPx)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WrapColumn {
                AppText(
                    text = sunriseSunsetDesc,
                    fontSize = 14.sp,
                    color = colorResource(R.color.color_white)
                )
                VerticalSpace(height = 8.dp)
                AppText(
                    text = sunriseSunsetTime,
                    fontSize = 17.sp,
                    color = colorResource(R.color.color_white),
                    fontWeight = FontWeight.Medium
                )
            }
            WrapColumn {
                WeatherObserveSunriseSunsetChart(
                    panelWidth = 72.dp,
                    panelHeight = 42.dp,
                    sunrise = currentWeatherDetailData?.sunrise ?: "",
                    sunset = currentWeatherDetailData?.sunset ?: ""
                )
                VerticalSpace(height = 4.dp)
                AppRow(
                    modifier = Modifier.width(72.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText(
                        text = currentWeatherDetailData?.sunrise ?: "",
                        fontSize = 10.sp,
                        color = colorResource(R.color.color_white)
                    )
                    AppText(
                        text = currentWeatherDetailData?.sunset ?: "",
                        fontSize = 10.sp,
                        color = colorResource(R.color.color_white)
                    )
                }
            }
        }
    }
}
