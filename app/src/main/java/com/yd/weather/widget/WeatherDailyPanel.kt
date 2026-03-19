package com.yd.weather.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherData
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.Commons
import com.yd.weather.utils.MMKVUtils
import com.yd.weather.utils.WeatherIconUtils
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.formatDateStr
import com.yd.weather.utils.isToday
import kotlin.Boolean
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeatherDailyPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    enable: Boolean = true
) {
    var currentDailyWeatherType by remember {
        mutableStateOf(
            MMKVUtils.getString(Constants.CURRENT_DAILY_WEATHER_TYPE, Constants.LIST_DAILY_WEATHER)
        )
    }
    var isExpand by remember { mutableStateOf(false) }
    val weatherData = item.weatherData
    var panelHeight: Int
    if (currentDailyWeatherType == Constants.LINE_CHART_DAILY_WEATHER) {
        val find = weatherData?.forecast15?.find { !it.aqiLevelName.isNullOrEmpty() }
        panelHeight = if (find != null) 420 else 394
    } else {
        val value = if (isExpand) max(
            Constants.DAILY_WEATHER_ITEM_COUNT,
            weatherData?.forecast15?.size ?: Constants.DAILY_WEATHER_ITEM_COUNT
        ) else min(
            Constants.DAILY_WEATHER_ITEM_COUNT,
            weatherData?.forecast15?.size ?: Constants.DAILY_WEATHER_ITEM_COUNT
        )
        panelHeight = Constants.ITEM_STICKY_HEIGHT + Constants.DAILY_WEATHER_ITEM_HEIGHT * value +
                Constants.DAILY_WEATHER_BOTTOM_HEIGHT
    }

    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = panelHeight,
        animateContentSize = true,
        stickyTitle = "15日天气预报",
        rightStickContent = {
            RightStickContent(
                currentDailyWeatherType = currentDailyWeatherType,
                enable = enable,
                lineChartButtonClick = {
                    if (!enable) return@RightStickContent
                    if (currentDailyWeatherType == Constants.LINE_CHART_DAILY_WEATHER) return@RightStickContent
                    MMKVUtils.putString(
                        Constants.CURRENT_DAILY_WEATHER_TYPE, Constants.LINE_CHART_DAILY_WEATHER
                    )
                    currentDailyWeatherType = Constants.LINE_CHART_DAILY_WEATHER
                },
                listButtonClick = {
                    if (!enable) return@RightStickContent
                    if (currentDailyWeatherType == Constants.LIST_DAILY_WEATHER) return@RightStickContent
                    MMKVUtils.putString(
                        Constants.CURRENT_DAILY_WEATHER_TYPE, Constants.LIST_DAILY_WEATHER
                    )
                    currentDailyWeatherType = Constants.LIST_DAILY_WEATHER
                }
            )
        }
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
            // 曲线
            LineChartDailyWeather(
                forecast15 = weatherData?.forecast15,
                weatherData = weatherData,
                visible = currentDailyWeatherType == Constants.LINE_CHART_DAILY_WEATHER
            )
            // 列表
            ListDailyWeather(
                forecast15 = weatherData?.forecast15,
                weatherData = weatherData,
                visible = currentDailyWeatherType == Constants.LIST_DAILY_WEATHER,
                isExpand = isExpand,
                lookMore = {
                    isExpand = !isExpand
                }
            )
        }
    }
}

@Composable
internal fun RightStickContent(
    currentDailyWeatherType: String = Constants.LIST_DAILY_WEATHER,
    enable: Boolean = true,
    lineChartButtonClick: () -> Unit,
    listButtonClick: () -> Unit
) {
    WrapRow {
        AppText(
            modifier = Modifier
                .then(if (enable) Modifier.alphaClick(onClick = lineChartButtonClick) else Modifier)
                .height(Constants.ITEM_STICKY_HEIGHT.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 12.dp),
            text = "曲线",
            fontSize = 11.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (currentDailyWeatherType == Constants.LINE_CHART_DAILY_WEATHER) 1f else 0.6f),
            textAlign = TextAlign.Center
        )
        VerticalDivider(
            modifier = Modifier.height(12.dp),
            thickness = 0.5.dp,
            color = colorResource(R.color.color_white).copy(alpha = 0.6f)
        )
        AppText(
            modifier = Modifier
                .then(if (enable) Modifier.alphaClick(onClick = listButtonClick) else Modifier)
                .height(Constants.ITEM_STICKY_HEIGHT.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(horizontal = 12.dp),
            text = "列表",
            fontSize = 11.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (currentDailyWeatherType == Constants.LIST_DAILY_WEATHER) 1f else 0.6f),
            textAlign = TextAlign.Center
        )
        HorizontalSpace(width = 4.dp)
    }
}

@Composable
internal fun LineChartDailyWeather(
    forecast15: List<WeatherDetailData>? = null,
    weatherData: WeatherData? = null,
    visible: Boolean = true
) {
    if (forecast15.isNullOrEmpty()) return
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        LazyRow(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(forecast15) { index, item ->
                LineChartDailyWeatherItem(item, index, weatherData)
            }
        }
    }
}

@Composable
internal fun ListDailyWeather(
    forecast15: List<WeatherDetailData>? = null,
    weatherData: WeatherData? = null,
    visible: Boolean = true,
    isExpand: Boolean = true,
    lookMore: () -> Unit
) {
    if (forecast15.isNullOrEmpty()) return
    val rotation by animateFloatAsState(
        targetValue = if (isExpand) -180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        AppColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = false
            ) {
                items(forecast15) { item ->
                    ListDailyWeatherItem(item, weatherData)
                }
            }
            WrapRow(
                modifier = Modifier
                    .alphaClick(onClick = lookMore)
                    .width(88.dp)
                    .height((Constants.DAILY_WEATHER_BOTTOM_HEIGHT - 1).dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AppText(
                    text = if (isExpand) "收起" else "展开",
                    fontSize = 11.sp,
                    color = colorResource(R.color.color_white).copy(alpha = 0.6f)
                )
                HorizontalSpace(width = 8.dp)
                CommonIcon(
                    modifier = Modifier
                        .alpha(0.6f)
                        .graphicsLayer(rotationZ = rotation),
                    resId = R.mipmap.ic_expand_icon,
                    size = 10.dp,
                    tint = colorResource(R.color.color_white)
                )
            }
        }
    }
}

@Composable
internal fun LineChartDailyWeatherItem(
    item: WeatherDetailData,
    index: Int = 0,
    weatherData: WeatherData?
) {
    val isBefore = Commons.isBefore(item.date)
    val maxTempData = weatherData?.forecast15?.maxByOrNull { it.high }
    val minTempData = weatherData?.forecast15?.minByOrNull { it.low }
    WrapColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalSpace(height = 12.dp)
        AppText(
            text = Commons.getWeatherDateTime(item.date),
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = formatDateStr(item.date, "MM/dd") ?: "",
            fontSize = 11.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = item.day?.wthr ?: "",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        VerticalSpace(height = 12.dp)
        CommonIcon(
            modifier = Modifier.alpha(if (isBefore) 0.5f else 1f),
            resId = WeatherIconUtils.getWeatherIconByType(
                item.day?.type ?: -1, item.day?.thirdType ?: "", false
            ),
            size = 24.dp,
            tint = Color.Unspecified
        )
        WeatherDailyTempPanel(
            panelWidth = 68.5.dp,
            panelHeight = 124.dp,
            preData = weatherData?.forecast15?.getOrNull(index - 1),
            data = item,
            nextData = weatherData?.forecast15?.getOrNull(index + 1),
            maxTemp = maxTempData?.high ?: 0,
            minTemp = minTempData?.low ?: 0
        )
        CommonIcon(
            modifier = Modifier.alpha(if (isBefore) 0.5f else 1f),
            resId = WeatherIconUtils.getWeatherIconByType(
                item.night?.type ?: -1, item.night?.thirdType ?: "", true
            ),
            size = 24.dp,
            tint = Color.Unspecified
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = item.night?.wthr ?: "",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = item.wd ?: "",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = item.wp ?: "",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 0.5f else 1f)
        )
        if (!item.aqiLevelName.isNullOrEmpty()) {
            VerticalSpace(height = 8.dp)
            AppText(
                modifier = Modifier
                    .background(
                        colorResource(Commons.getAqiColor(item.aqi)).copy(alpha = 0.48f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                text = item.aqiLevelName,
                fontSize = 10.sp,
                color = colorResource(R.color.color_white)
            )
        }
    }
}

@Composable
internal fun ListDailyWeatherItem(
    item: WeatherDetailData,
    weatherData: WeatherData?
) {
    val isBefore = Commons.isBefore(item.date)
    val maxTempData = weatherData?.forecast15?.maxByOrNull { it.high }
    val minTempData = weatherData?.forecast15?.minByOrNull { it.low }
    WrapColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.DAILY_WEATHER_ITEM_HEIGHT.dp)
            .padding(horizontal = 16.dp)
    ) {
        AppRow(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isBefore) 0.5f else 1f)
        ) {
            WrapColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                AppText(
                    text = Commons.getWeatherDateTime(item.date),
                    fontSize = 16.sp,
                    color = colorResource(R.color.color_white)
                )
                VerticalSpace(height = 2.dp)
                AppText(
                    text = formatDateStr(item.date, "MM/dd") ?: "",
                    fontSize = 11.sp,
                    color = colorResource(R.color.color_white)
                )
            }
            HorizontalSpace(width = 38.dp)
            CommonIcon(
                resId = WeatherIconUtils.getWeatherIconByType(
                    item.day?.type ?: -1, item.day?.thirdType ?: "", false
                ),
                size = 24.dp,
                tint = Color.Unspecified
            )
            HorizontalSpace(width = 38.dp)
            AppText(
                modifier = Modifier.width(42.dp),
                text = Commons.getTemp(item.low),
                fontSize = 17.sp,
                color = colorResource(R.color.color_white).copy(alpha = if (isBefore) 1f else 0.5f),
                textAlign = TextAlign.Start
            )
            WeatherTempLineBar(
                modifier = Modifier.weight(1f),
                barHeight = 4.dp,
                temp = if (isToday(item.date)) weatherData?.observe?.temp else null,
                high = item.high,
                low = item.low,
                maxTemp = maxTempData?.high ?: 0,
                minTemp = minTempData?.low ?: 0
            )
            HorizontalSpace(width = 16.dp)
            AppText(
                text = Commons.getTemp(item.high),
                fontSize = 17.sp,
                color = colorResource(R.color.color_white)
            )
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = colorResource(R.color.color_white).copy(alpha = 0.2f)
        )
    }
}