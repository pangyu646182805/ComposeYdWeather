package com.yd.weather.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppScaffold
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.utils.SetStatusBarStyle
import com.yd.weather.utils.isExpandedWidth
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    mainViewModel: MainViewModel = hiltViewModel(),
    cityManagerViewModel: CityManagerViewModel = hiltViewModel()
) {
    val viewState by mainViewModel.viewState.collectAsState()
    val isShowWeatherPage by mainViewModel.isShowWeatherPage.collectAsStateWithLifecycle()
    val weatherItems by mainViewModel.weatherItems.collectAsStateWithLifecycle()
    val itemTypeObserves by mainViewModel.itemTypeObserves.collectAsStateWithLifecycle()
    val addedCities by mainViewModel.addedCityData.collectAsStateWithLifecycle()
    val weatherBg by mainViewModel.weatherBg.collectAsStateWithLifecycle()
    val isWeatherHeaderDark by mainViewModel.isWeatherHeaderDark.collectAsStateWithLifecycle()
    val isDark by mainViewModel.isDark.collectAsStateWithLifecycle()
    val panelOpacity by mainViewModel.panelOpacity.collectAsStateWithLifecycle()
    val currentCityData by mainViewModel.appState().currentCityData.collectAsStateWithLifecycle()

    MainScreen(
        viewState = viewState,
        isShowWeatherPage = isShowWeatherPage,
        weatherItems = weatherItems,
        itemTypeObserves = itemTypeObserves,
        weatherBg = weatherBg,
        isWeatherHeaderDark = isWeatherHeaderDark,
        isDark = isDark,
        panelOpacity = panelOpacity,
        addedCities = addedCities,
        currentCityData = currentCityData,
        mainViewModel = mainViewModel,
        cityManagerViewModel = cityManagerViewModel
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainScreen(
    viewState: ViewState = ViewState.Loading,
    isShowWeatherPage: Boolean = true,
    weatherItems: List<WeatherItemData>? = null,
    itemTypeObserves: Array<Int>? = null,
    weatherBg: List<Color> = emptyList(),
    isWeatherHeaderDark: Boolean = false,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    addedCities: List<CityData>? = null,
    currentCityData: CityData? = null,
    mainViewModel: MainViewModel = hiltViewModel(),
    cityManagerViewModel: CityManagerViewModel = hiltViewModel()
) {
    val cityManagerScrollState = rememberLazyListState()

    // 大屏（平板 / 折叠展开）走 List-Detail；手机保持原有单页布局完全不变
    if (isExpandedWidth()) {
        SetStatusBarStyle(isLight = !isWeatherHeaderDark)
        TabletMainScreen(
            viewState = viewState,
            weatherItems = weatherItems,
            itemTypeObserves = itemTypeObserves,
            weatherBg = weatherBg,
            isWeatherHeaderDark = isWeatherHeaderDark,
            isDark = isDark,
            panelOpacity = panelOpacity,
            addedCities = addedCities,
            currentCityData = currentCityData,
            mainViewModel = mainViewModel,
            cityManagerViewModel = cityManagerViewModel
        )
        return
    }

    // 城市管理页的底色是 bg_color，夜间模式下是 #18191A（近黑）。
    // isAppearanceLightStatusBars 说的是"状态栏背景是浅色"，系统据此把图标画成黑色，
    // 所以这里写死 true 会让夜间模式下黑底配黑图标，状态栏整片看不见。
    SetStatusBarStyle(
        isLight = if (isShowWeatherPage) !isWeatherHeaderDark else !isSystemInDarkTheme()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CityManagerPage(
            isShowWeatherPage = isShowWeatherPage,
            scrollState = cityManagerScrollState,
            addedCities = addedCities,
            mainViewModel = mainViewModel,
            viewModel = cityManagerViewModel
        )

        WeatherPage(
            viewState = viewState,
            cityManagerScrollState = cityManagerScrollState,
            isShowWeatherPage = isShowWeatherPage,
            addedCities = addedCities,
            weatherBg = weatherBg,
            isWeatherHeaderDark = isWeatherHeaderDark,
            isDark = isDark,
            panelOpacity = panelOpacity,
            weatherItems = weatherItems,
            itemTypeObserves = itemTypeObserves,
            currentCityData = currentCityData,
            mainViewModel = mainViewModel,
            cityManagerViewModel = cityManagerViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YdWeatherAppTheme {
        MainScreen()
    }
}