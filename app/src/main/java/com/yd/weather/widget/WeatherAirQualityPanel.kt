package com.yd.weather.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.dialog.AirQualityDetailPopup
import com.yd.weather.dialog.AirQualityQueryDialog
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.WeatherPanelClip

@Composable
fun WeatherAirQualityPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    enable: Boolean = true,
    showHideWeatherContent: ((Boolean) -> Unit)? = null
) {
    var showAirQualityDialog by remember { mutableStateOf(false) }
    var showDetailPopup by remember { mutableStateOf(false) }
    var panelYPx by remember { mutableFloatStateOf(0f) }
    val weatherData = item.weatherData

    if (enable && showAirQualityDialog) {
        AirQualityQueryDialog(onDismiss = { showAirQualityDialog = false })
    }

    if (enable && showDetailPopup) {
        AirQualityDetailPopup(
            evn = weatherData?.evn,
            isDark = isDark,
            panelOpacity = panelOpacity,
            panelYPx = panelYPx,
            onQueryClick = {
                showAirQualityDialog = true
            },
            onDismiss = {
                showDetailPopup = false
                showHideWeatherContent?.invoke(true)
            }
        )
    }

    WeatherStickyPanel(
        modifier = if (enable) Modifier
            .onGloballyPositioned { coordinates ->
                panelYPx = coordinates.positionOnScreen().y
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showHideWeatherContent?.invoke(false)
                showDetailPopup = true
            } else Modifier,
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT,
        stickyTitle = "空气质量",
        supportTitleOpacity = true
    ) { offsetPx, titleOpacity, _ ->
        AppColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT.dp)
                .padding(horizontal = 16.dp)
                .clip(WeatherPanelClip(offsetPx))
        ) {
            VerticalSpace(height = 12.dp)
            WrapRow(modifier = Modifier.alpha(titleOpacity)) {
                AppText(
                    text = "${weatherData?.evn?.aqi} - ${weatherData?.evn?.aqiLevelName}",
                    fontSize = 16.sp,
                    color = colorResource(R.color.color_white),
                    fontWeight = FontWeight.Medium
                )
                CommonIcon(
                    modifier = (if (enable) Modifier.alphaClick { showAirQualityDialog = true } else Modifier)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    resId = R.mipmap.ic_query_icon,
                    size = 14.dp,
                    tint = colorResource(R.color.color_white)
                )
            }
            VerticalSpace(height = 12.dp)
            AirQualityBar(barHeight = 4.dp, aqi = weatherData?.evn?.aqi ?: 0)
            VerticalSpace(height = 10.dp)
            AppText(
                text = "当前AQI为${weatherData?.evn?.aqi}",
                fontSize = 13.sp,
                color = colorResource(R.color.color_white)
            )
        }
    }
}
