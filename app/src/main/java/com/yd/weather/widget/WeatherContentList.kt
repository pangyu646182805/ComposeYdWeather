package com.yd.weather.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData

@Composable
fun WeatherContentList(
    weatherScrollState: LazyListState = rememberLazyListState(),
    isShowWeatherPage: Boolean = true,
    animValue: Float = 0f,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    isWeatherHeaderDark: Boolean = false,
    currentCityData: CityData? = null,
    weatherItems: List<WeatherItemData>? = null,
    itemTypeObserves: Array<Int>? = null,
    showSortCardButton: Boolean = true,
    previewCity: Boolean = false
) {
    val weatherItemsFilter =
        weatherItems?.filter { it.itemType != Constants.ITEM_TYPE_WEATHER_HEADER }
    val weatherHeaderItemData =
        weatherItems?.find { it.itemType == Constants.ITEM_TYPE_WEATHER_HEADER }
    val sourceTitle = weatherItemsFilter?.firstOrNull()?.weatherData?.source?.title

    val density = LocalDensity.current

    val firstItemOffset by remember {
        derivedStateOf { with(density) { weatherScrollState.firstVisibleItemScrollOffset.toDp().value } }
    }
    val firstVisibleItemIndex by remember {
        derivedStateOf { weatherScrollState.firstVisibleItemIndex }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(
                if (isShowWeatherPage)
                    1 - ((animValue - 0.8f) / 0.2f).coerceIn(0f, 1f)
                else
                    ((0.2f - animValue) / 0.2f).coerceIn(0f, 1f)
            )
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    top = Constants.WEATHER_HEADER_MIN_HEIGHT.dp,
                    start = Constants.ITEM_PANEL_MARGIN.dp,
                    end = Constants.ITEM_PANEL_MARGIN.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = Constants.ITEM_PANEL_RADIUS.dp,
                            topEnd = Constants.ITEM_PANEL_RADIUS.dp
                        )
                    ),
                state = weatherScrollState
            ) {
                item {
                    VerticalSpace(height = (Constants.WEATHER_HEADER_MAX_HEIGHT - Constants.WEATHER_HEADER_MIN_HEIGHT).dp)
                }
                if (!weatherItemsFilter.isNullOrEmpty()) {
                    itemsIndexed(
                        weatherItemsFilter,
                        key = { _, item -> item.itemType }) { index, item ->
                        when (item.itemType) {
                            Constants.ITEM_TYPE_ALARMS -> WeatherAlarmsPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_AIR_QUALITY -> WeatherAirQualityPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_HOUR_WEATHER -> WeatherHourPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_DAILY_WEATHER -> WeatherDailyPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_OBSERVE -> WeatherObservePanel(
                                item = item,
                                itemTypeObserves = itemTypeObserves,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_LIFE_INDEX -> WeatherLifeIndexPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )
                        }
                        VerticalSpace(height = 12.dp)
                    }
                }
                if (!sourceTitle.isNullOrEmpty()) {
                    item {
                        WeatherFooter(
                            sourceTitle,
                            isDark = isDark,
                            showSortCardButton = showSortCardButton
                        )
                    }
                }
            }
        }
        WeatherHeaderWidget(
            currentCityData = currentCityData,
            weatherHeaderOffset = firstItemOffset,
            firstVisibleItemIndex = firstVisibleItemIndex,
            isWeatherHeaderDark = isWeatherHeaderDark,
            weatherItemData = weatherHeaderItemData,
            previewCity = previewCity
        )
    }
}