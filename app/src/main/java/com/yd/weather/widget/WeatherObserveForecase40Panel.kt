package com.yd.weather.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.dialog.WeatherForecase40DetailPopup
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.WeatherPanelClip

@Composable
fun WeatherObserveForecase40Panel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    isWeatherHeaderDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    showHideWeatherContent: ((Boolean) -> Unit)? = null
) {
    var showDetailPopup by remember { mutableStateOf(false) }
    var panelYPx by remember { mutableFloatStateOf(0f) }
    var panelWidthPx by remember { mutableFloatStateOf(0f) }
    var panelXPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val weatherData = item.weatherData

    if (showDetailPopup) {
        val panelAlignment =
            if (panelXPx < screenWidthPx / 2) Alignment.Start else Alignment.End
        WeatherForecase40DetailPopup(
            forecast40 = weatherData?.forecast40,
            forecast40Data = weatherData?.forecast40V2,
            cityName = weatherData?.meta?.city,
            isDark = isDark,
            isWeatherHeaderDark = isWeatherHeaderDark,
            panelOpacity = panelOpacity,
            panelYPx = panelYPx,
            panelWidthPx = panelWidthPx,
            panelAlignment = panelAlignment,
            onDismiss = {
                showDetailPopup = false
                showHideWeatherContent?.invoke(true)
            }
        )
    }

    WeatherStickyPanel(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                panelYPx = coordinates.positionOnScreen().y
                panelWidthPx = coordinates.size.width.toFloat()
                panelXPx = coordinates.positionOnScreen().x
            },
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "40日预报"
    ) { offsetPx, _, _ ->
        AppColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
                .clip(WeatherPanelClip(offsetPx))
                .then(
                    if (showHideWeatherContent != null) Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        showHideWeatherContent.invoke(false)
                        showDetailPopup = true
                    } else Modifier
                ),
            verticalArrangement = Arrangement.Center
        ) {
            val upDays = weatherData?.forecast40V2?.upDays ?: 0
            val rainDays = weatherData?.forecast40V2?.rainDays ?: 0
            val upDaysDesc = if (upDays > 0) "${upDays}天升温" else "预计近期气温平稳"
            val rainDaysDesc = if (rainDays > 0) "${rainDays}天有雨" else "预计近期无降雨"

            VerticalSpace(height = 20.dp)
            AppText(
                text = "温度趋势",
                fontSize = 12.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f)
            )
            VerticalSpace(height = 4.dp)
            AppText(
                text = upDaysDesc,
                fontSize = 14.sp,
                color = colorResource(R.color.color_white)
            )
            VerticalSpace(height = 12.dp)
            AppText(
                text = "降水趋势",
                fontSize = 12.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f)
            )
            VerticalSpace(height = 4.dp)
            AppText(
                text = rainDaysDesc,
                fontSize = 14.sp,
                color = colorResource(R.color.color_white)
            )
        }
    }
}
