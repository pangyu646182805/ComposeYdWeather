package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.NoPaddingText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.Commons
import com.yd.weather.utils.WeatherPanelClip
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun WeatherObserveTiGanPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData
    val tiGan = (weatherData?.observe?.tiGan ?: "0").toFloatOrNull() ?: 0f
    val tiGanTemp = round(tiGan).roundToInt()
    val actualTemp = weatherData?.observe?.temp ?: 0
    val diff = tiGanTemp - actualTemp
    val tiGanTempDesc = when {
        kotlin.math.abs(diff) < 2 -> "与实际温度相似"
        diff > 0 -> "比实际温度高${Commons.getTemp(diff)}"
        else -> "比实际温度低${Commons.getTemp(-diff)}"
    }

    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "体感温度"
    ) { offsetPx, _, _ ->
        AppColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
                .clip(WeatherPanelClip(offsetPx)),
            verticalArrangement = Arrangement.Center
        ) {
            VerticalSpace(height = 20.dp)
            NoPaddingText(
                text = Commons.getTemp(tiGanTemp),
                fontSize = 32.sp,
                color = colorResource(R.color.color_white)
            )
            VerticalSpace(height = 30.dp)
            AppText(
                text = tiGanTempDesc,
                fontSize = 14.sp,
                color = colorResource(R.color.color_white)
            )
        }
    }
}