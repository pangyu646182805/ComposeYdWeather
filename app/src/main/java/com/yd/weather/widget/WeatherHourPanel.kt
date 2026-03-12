package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.yd.weather.R
import com.yd.weather.config.Constants

@Composable
fun WeatherHourPanel(
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.WEATHER_HOUR_PANEL_HEIGHT.dp)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
            )
    ) {

    }
}