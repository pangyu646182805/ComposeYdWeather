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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.RefreshState
import com.yd.weather.utils.rememberRefreshState

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
    previewCity: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onRefreshState: ((RefreshState) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshState = rememberRefreshState(coroutineScope).apply {
        headerHeight = 128f
        this.onRefresh = onRefresh
        enableRefresh = onRefresh != null
    }

    // 把 refreshState 暴露给调用方，用于调用 refreshComplete()
    onRefreshState?.invoke(refreshState)

    val refreshOffset by remember {
        derivedStateOf { refreshState.indicatorOffset }
    }

    // 刷新触发阈值
    val refreshTriggerOffset = 128f

    // 参照 Dart: refreshing/complete 时 opacity=1，否则按下拉比例计算
    val baseRefreshOpacity by remember {
        derivedStateOf {
            if (refreshState.isRefreshing || refreshState.isFinishing) 1f
            else (refreshState.indicatorOffset / refreshTriggerOffset).coerceIn(0f, 1f)
        }
    }
    // 刷新完成时 opacity 从1平滑过渡到0
    val refreshOpacity by animateFloatAsState(
        targetValue = if (refreshState.isFinishing) 0f else baseRefreshOpacity,
        animationSpec = tween(if (refreshState.isFinishing) 600 else 0),
        label = "refreshOpacity"
    )

    val refreshDesc = when {
        refreshState.isFinishing -> "刷新完成"
        refreshState.isRefreshing -> "正在刷新"
        else -> "释放刷新"
    }

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
                .graphicsLayer { translationY = refreshOffset }
                .padding(
                    top = Constants.WEATHER_HEADER_MIN_HEIGHT.dp,
                    start = Constants.ITEM_PANEL_MARGIN.dp,
                    end = Constants.ITEM_PANEL_MARGIN.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(refreshState.connection)
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
            weatherHeaderOffset = if (firstVisibleItemIndex <= 0 && firstItemOffset <= 0) -refreshOffset else firstItemOffset,
            firstVisibleItemIndex = firstVisibleItemIndex,
            isWeatherHeaderDark = isWeatherHeaderDark,
            weatherItemData = weatherHeaderItemData,
            previewCity = previewCity,
            refreshOpacity = refreshOpacity,
            refreshDesc = refreshDesc
        )
    }
}