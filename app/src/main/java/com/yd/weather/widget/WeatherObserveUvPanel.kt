package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.getToday
import com.yd.weather.utils.toDateString

@Composable
fun WeatherObserveUvPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData

    var uvIndex = weatherData?.observe?.uvIndex ?: 0
    var uvIndexMax = weatherData?.observe?.uvIndexMax ?: 0
    var uvLevel = weatherData?.observe?.uvLevel ?: ""
    if (uvIndex <= 0 || uvIndexMax <= 0 || uvLevel.isEmpty()) {
        val currentWeatherDetailData =
            weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
        uvIndex = currentWeatherDetailData?.uvIndex ?: 0
        uvIndexMax = currentWeatherDetailData?.uvIndexMax ?: 0
        uvLevel = currentWeatherDetailData?.uvLevel ?: "0"
    }
    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "紫外线指数"
    ) { offsetPx, _, _ ->
        AppRow(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .clip(WeatherPanelClip(offsetPx)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText(
                text = uvLevel,
                fontSize = 18.sp,
                color = colorResource(R.color.color_white),
                fontWeight = FontWeight.Medium
            )
            WeatherObserveUvChart(
                panelWidth = 68.dp,
                panelHeight = 68.dp,
                uvIndex = uvIndex,
                uvIndexMax = uvIndexMax
            )
        }
    }
}