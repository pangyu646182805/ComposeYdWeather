package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
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
    firstVisibleItemIndex: Int = 0
) {
    val density = LocalDensity.current
    val weatherData = item.weatherData
    val offset = when {
        index + 1 > firstVisibleItemIndex -> 0f
        index + 1 == firstVisibleItemIndex -> firstItemOffset
        else -> Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT.toFloat()
    }
    val percent =
        ((offset - (Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT)) / Constants.ITEM_STICKY_HEIGHT)
            .coerceIn(0f, 1f)
    val contentOpacity = 1 - percent
    var stickyTranslateY =
        if (offset > Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT)
            (Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT).toFloat()
        else
            offset
    stickyTranslateY += percent * Constants.ITEM_STICKY_HEIGHT * 0.5f
    val titleOpacity = (1 - offset / 12).coerceIn(0f, 1f)

    val offsetPx = with(density) {
        (offset + Constants.ITEM_STICKY_HEIGHT * (offset / Constants.ITEM_STICKY_HEIGHT).coerceIn(
            0f, 1f
        )).dp.toPx()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT.dp)
            .alpha(contentOpacity)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
            )
    ) {
        AppText(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.ITEM_STICKY_HEIGHT.dp)
                .graphicsLayer(
                    alpha = 1 - titleOpacity,
                    translationY = with(density) { stickyTranslateY.dp.toPx() })
                .padding(start = Constants.ITEM_PANEL_MARGIN.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            text = "空气质量",
            fontSize = 12.sp,
            color = colorResource(R.color.color_white).copy(alpha = 0.6f),
            textAlign = TextAlign.Start
        )
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
                    fontWeight = FontWeight.Bold
                )
                CommonIcon(
                    modifier = Modifier
                        .alphaClick {}
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