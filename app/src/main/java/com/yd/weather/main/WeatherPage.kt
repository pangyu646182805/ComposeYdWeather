package com.yd.weather.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.yd.weather.component.CenterTopAppBar
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.WeatherContentClip
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel
import com.yd.weather.widget.WeatherAirQualityPanel
import com.yd.weather.widget.WeatherAlarmsPanel
import com.yd.weather.widget.WeatherDailyPanel
import com.yd.weather.widget.WeatherHeaderWidget
import com.yd.weather.widget.WeatherHourPanel
import com.yd.weather.widget.WeatherLifeIndexPanel
import com.yd.weather.widget.WeatherObservePanel

@Composable
fun WeatherPage(
    viewState: ViewState = ViewState.Loading,
    cityManagerScrollState: LazyListState = rememberLazyListState(),
    isShowWeatherPage: Boolean = true,
    addedCities: List<CityData>? = null,
    weatherBg: List<Color> = emptyList(),
    isWeatherHeaderDark: Boolean = false,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    weatherItems: List<WeatherItemData>? = null,
    currentCityData: CityData? = null,
    mainViewModel: MainViewModel = hiltViewModel(),
    cityManagerViewModel: CityManagerViewModel = hiltViewModel()
) {
    val weatherScrollState = rememberLazyListState()
    val animValue by animateFloatAsState(
        targetValue = if (isShowWeatherPage) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animValue",
        finishedListener = {
            println("finishedListener: $it")
            if (isShowWeatherPage) {
                cityManagerViewModel.hideCityList()
            } else {
                cityManagerViewModel.showCityList(addedCities, cityManagerScrollState)
            }
        }
    )
    if (animValue < 1) {
        val startColor by animateColorAsState(
            targetValue = weatherBg[0],
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "startColor"
        )
        val endColor by animateColorAsState(
            targetValue = weatherBg[1],
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "endColor"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures()
                }
                .alpha(1 - ((animValue - 0.95f) / 0.05f).coerceIn(0f, 1f))
                .clip(WeatherContentClip(animValue, mainViewModel.offsetY))
                .background(
                    brush = Brush.verticalGradient(colors = listOf(startColor, endColor))
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val isSystemInDarkTheme = isSystemInDarkTheme()
                if (isSystemInDarkTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        colorResource(R.color.color_black).copy(alpha = 0.25f),
                                        colorResource(R.color.color_black).copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }
                MultipleStatusView(
                    viewState = viewState,
                    loadingColor = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                ) {
                    WeatherContentList(
                        weatherScrollState = weatherScrollState,
                        isShowWeatherPage = isShowWeatherPage,
                        animValue = animValue,
                        isDark = isDark,
                        isWeatherHeaderDark = isWeatherHeaderDark,
                        currentCityData = currentCityData,
                        weatherItems = weatherItems
                    )
                }
                CenterTopAppBar(
                    showBackIcon = false,
                    colors = topAppBarColors(containerColor = colorResource(R.color.transparent)),
                    actions = {
                        RightIcon(isWeatherHeaderDark = isWeatherHeaderDark) {
                            mainViewModel.showCityManagerPage(
                                cityManagerViewModel, cityManagerScrollState
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun WeatherContentList(
    weatherScrollState: LazyListState = rememberLazyListState(),
    isShowWeatherPage: Boolean = true,
    animValue: Float = 0f,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    isWeatherHeaderDark: Boolean = false,
    currentCityData: CityData? = null,
    weatherItems: List<WeatherItemData>? = null
) {
    val weatherItemsFilter =
        weatherItems?.filter { it.itemType != Constants.ITEM_TYPE_WEATHER_HEADER }
    val weatherHeaderItemData =
        weatherItems?.find { it.itemType == Constants.ITEM_TYPE_WEATHER_HEADER }

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
                state = weatherScrollState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )

                            Constants.ITEM_TYPE_LIFE_INDEX -> WeatherLifeIndexPanel(
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = firstItemOffset,
                                firstVisibleItemIndex = firstVisibleItemIndex
                            )
                        }
                    }
                }
            }
        }
        WeatherHeaderWidget(
            currentCityData = currentCityData,
            weatherHeaderOffset = firstItemOffset,
            firstVisibleItemIndex = firstVisibleItemIndex,
            isWeatherHeaderDark = isWeatherHeaderDark,
            weatherItemData = weatherHeaderItemData
        )
    }
}

@Composable
fun RightIcon(isWeatherHeaderDark: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        CommonIcon(
            resId = R.mipmap.ic_add,
            size = 20.dp,
            tint = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
        )
    }
}