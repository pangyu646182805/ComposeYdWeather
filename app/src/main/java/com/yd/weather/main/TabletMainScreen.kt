package com.yd.weather.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.StartAlignColumn
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.routes.CardSortRoutes
import com.yd.weather.utils.Commons
import com.yd.weather.utils.RefreshState
import com.yd.weather.utils.getToday
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel
import com.yd.weather.widget.WeatherContentList

/** 左侧城市列表面板宽度（设计稿约 360dp）。 */
private val CITY_PANE_WIDTH = 360.dp

/**
 * 平板 / 折叠屏展开态主界面：左城市列表 + 右天气面板（List-Detail）。
 *
 * 阶段 1a（骨架）：右栏先复用现有 [WeatherContentList] 单列内容，跑通左右分栏与切城市；
 * 双列瀑布流与平板头部留待阶段 1b。
 *
 * **约束**：本组件只在 [com.yd.weather.utils.isExpandedWidth] 为 true 时被调用，手机端不会进入这里。
 */
@Composable
fun TabletMainScreen(
    viewState: ViewState = ViewState.Loading,
    weatherItems: List<WeatherItemData>? = null,
    itemTypeObserves: Array<Int>? = null,
    weatherBg: List<Color> = emptyList(),
    isWeatherHeaderDark: Boolean = false,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    addedCities: List<CityData>? = null,
    currentCityData: CityData? = null,
    mainViewModel: MainViewModel,
    cityManagerViewModel: CityManagerViewModel
) {
    val weatherScrollState = rememberLazyListState()
    val refreshStateRef = remember { mutableStateOf<RefreshState?>(null) }

    // weatherBg 在首帧加载完成前可能为空，做兜底，避免 verticalGradient 崩溃
    val bgColors = if (weatherBg.size >= 2) {
        weatherBg
    } else {
        listOf(Color(0xFFB7C3CE), Color(0xFF8A9AA8))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = bgColors))
    ) {
        if (isSystemInDarkTheme()) {
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

        Row(modifier = Modifier.fillMaxSize()) {
            TabletCityListPane(
                modifier = Modifier
                    .width(CITY_PANE_WIDTH)
                    .fillMaxHeight(),
                addedCities = addedCities,
                currentCityData = currentCityData,
                appState = mainViewModel.appState(),
                isWeatherHeaderDark = isWeatherHeaderDark,
                onCityClick = { city ->
                    val appState = mainViewModel.appState()
                    val current = appState.currentCityData.value
                    val isSame = city.cityId == current?.cityId &&
                            city.isLocationCity == (current?.isLocationCity ?: false)
                    if (!isSame) appState.setCurrentCityData(city)
                },
                onAddClick = { cityManagerViewModel.toSelectCityPage() }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MultipleStatusView(
                    viewState = viewState,
                    loadingColor = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                ) {
                    WeatherContentList(
                        weatherScrollState = weatherScrollState,
                        isShowWeatherPage = true,
                        animValue = 0f,
                        isDark = isDark,
                        panelOpacity = panelOpacity,
                        isWeatherHeaderDark = isWeatherHeaderDark,
                        weatherBg = bgColors,
                        currentCityData = currentCityData,
                        weatherItems = weatherItems,
                        itemTypeObserves = itemTypeObserves,
                        onRefresh = {
                            mainViewModel.refreshWeatherData { refreshStateRef.value?.refreshComplete() }
                        },
                        onRefreshState = { refreshStateRef.value = it },
                        onCardSortButtonClick = {
                            mainViewModel.navigate(CardSortRoutes.CardSort)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletCityListPane(
    modifier: Modifier = Modifier,
    addedCities: List<CityData>? = null,
    currentCityData: CityData? = null,
    appState: AppState,
    isWeatherHeaderDark: Boolean = false,
    onCityClick: (CityData) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val titleColor = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black)

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.10f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 80.dp
            )
        ) {
            item {
                StartAlignColumn(fillMaxWidth = false) {
                    AppText(
                        text = "易得天气",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = titleColor
                    )
                    AppText(
                        text = "${addedCities?.size ?: 0} 城市",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Thin,
                        color = titleColor.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(addedCities?.size ?: 0) { index ->
                val city = addedCities?.getOrNull(index) ?: return@items
                val current = currentCityData
                val isSelected = city.cityId == current?.cityId &&
                        city.isLocationCity == (current?.isLocationCity ?: false)
                TabletCityRow(
                    city = city,
                    appState = appState,
                    isSelected = isSelected,
                    onClick = { onCityClick(city) }
                )
            }
        }

        // 底部「添加 / 搜索城市」按钮
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CommonIcon(
                    resId = R.mipmap.ic_search_icon,
                    size = 18.dp,
                    tint = titleColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppText(
                    text = "添加 / 搜索城市",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    color = titleColor
                )
            }
        }
    }
}

@Composable
private fun TabletCityRow(
    city: CityData,
    appState: AppState,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val cityBg = appState.generateWeatherBg(
        city.weatherData?.weatherType ?: "",
        Commons.isNight(getToday(), city.weatherData?.sunrise, city.weatherData?.sunset),
        true
    )
    val isDark = appState.isWeatherHeaderDark(cityBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.CITY_MANAGER_ITEM_HEIGHT.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Brush.verticalGradient(colors = cityBg))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = colorResource(R.color.color_white),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
    ) {
        if (isSystemInDarkTheme()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_black).copy(alpha = 0.3f),
                                colorResource(R.color.color_black).copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }
        // 复用手机端城市卡片内容
        CityItem(item = city, isEditMode = false, isDark = isDark)
    }
}
