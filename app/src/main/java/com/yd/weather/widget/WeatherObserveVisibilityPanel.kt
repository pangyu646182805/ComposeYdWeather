package com.yd.weather.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.yd.weather.utils.WeatherPanelClip
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun WeatherObserveVisibilityPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData
    val weatherVisibility = weatherData?.observe?.visibility ?: ""
    val upperVisibility = weatherVisibility.uppercase()

    val visibilityValue = when {
        upperVisibility.contains("KM") -> weatherVisibility.replace(Regex("[^\\d.]"), "")
            .toFloatOrNull() ?: 0f

        upperVisibility.contains("M") -> weatherVisibility.replace(Regex("[^\\d.]"), "")
            .toFloatOrNull() ?: 0f

        else -> 0f
    }

    val visibilityUnit = when {
        upperVisibility.contains("KM") -> "公里"
        upperVisibility.contains("M") -> "米"
        else -> ""
    }

    val visibilityDesc = when {
        weatherVisibility.isEmpty() -> ""
        upperVisibility.contains("KM") -> if (visibilityValue <= 1) "视野较差" else "视野非常好"
        upperVisibility.contains("M") -> if (visibilityValue <= 1000) "视野较差" else "视野非常好"
        else -> ""
    }

    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.ITEM_OBSERVE_PANEL_HEIGHT,
        stickyTitle = "能见度"
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
                text = "${round(visibilityValue).roundToInt()}$visibilityUnit",
                fontSize = 32.sp,
                color = colorResource(R.color.color_white)
            )
            VerticalSpace(height = 30.dp)
            AppText(
                text = visibilityDesc,
                fontSize = 14.sp,
                color = colorResource(R.color.color_white)
            )
        }
    }
}
