package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun WeatherObserveShiDuPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData
    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "湿度"
    ) { offsetPx, _, _ ->
        AppRow(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .clip(WeatherPanelClip(offsetPx)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText(
                text = weatherData?.observe?.shiDu ?: "",
                fontSize = 18.sp,
                color = colorResource(R.color.color_white),
                fontWeight = FontWeight.Medium
            )
            WeatherObserveShiDuChart(
                panelWidth = 68.dp,
                panelHeight = 68.dp,
                shiDu = weatherData?.observe?.shiDu ?: ""
            )
        }
    }
}