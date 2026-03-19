package com.yd.weather.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.yd.weather.component.CenterTopAppBar
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.db.model.CityData
import com.yd.weather.dialog.WeatherCitySelector
import com.yd.weather.model.WeatherItemData
import com.yd.weather.routes.CardSortRoutes
import kotlinx.coroutines.withTimeoutOrNull
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.RefreshState
import com.yd.weather.utils.WeatherContentClip
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel
import com.yd.weather.widget.WeatherContentList

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
    itemTypeObserves: Array<Int>? = null,
    currentCityData: CityData? = null,
    mainViewModel: MainViewModel = hiltViewModel(),
    cityManagerViewModel: CityManagerViewModel = hiltViewModel()
) {
    val weatherScrollState = rememberLazyListState()
    // 保存 refreshState 引用，用于数据加载完成后调用 refreshComplete()
    val refreshStateRef = remember { mutableStateOf<RefreshState?>(null) }
    var topBarOpacity by remember { mutableFloatStateOf(1f) }
    var showCitySelector by remember { mutableStateOf(false) }
    val animatable = remember { Animatable(if (isShowWeatherPage) 0f else 1f) }
    val predictiveBackProgress by mainViewModel.predictiveBackProgress.collectAsStateWithLifecycle()

    // 手势进行中 - snap 跟随手势进度
    LaunchedEffect(predictiveBackProgress) {
        val p = predictiveBackProgress
        if (p != null) {
            animatable.snapTo((1f - p).coerceIn(0f, 1f))
        }
    }

    // isShowWeatherPage 变化或手势结束 - spring 动画过渡到目标值
    LaunchedEffect(isShowWeatherPage, predictiveBackProgress == null) {
        if (predictiveBackProgress != null) return@LaunchedEffect
        val target = if (isShowWeatherPage) 0f else 1f
        if (animatable.value != target) {
            animatable.animateTo(target, spring(stiffness = Spring.StiffnessLow))
        }
        if (isShowWeatherPage) {
            cityManagerViewModel.hideCityList()
        } else {
            cityManagerViewModel.showCityList(addedCities, cityManagerScrollState)
        }
    }

    val animValue = animatable.value
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
                    detectTapGestures(
                        onLongPress = {
                            showCitySelector = true
                        }
                    )
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
                        panelOpacity = panelOpacity,
                        isWeatherHeaderDark = isWeatherHeaderDark,
                        currentCityData = currentCityData,
                        weatherItems = weatherItems,
                        itemTypeObserves = itemTypeObserves,
                        onRefresh = {
                            mainViewModel.refreshWeatherData { refreshStateRef.value?.refreshComplete() }
                        },
                        onRefreshState = { refreshStateRef.value = it },
                        onContentVisibilityChange = { show -> topBarOpacity = if (show) 1f else 0f },
                        onCardSortButtonClick = {
                            mainViewModel.navigate(CardSortRoutes.CardSort)
                        }
                    )
                }
                val animatedTopBarOpacity by animateFloatAsState(
                    targetValue = topBarOpacity,
                    animationSpec = tween(durationMillis = 200),
                    label = "topBarOpacity"
                )
                CenterTopAppBar(
                    modifier = Modifier.alpha(animatedTopBarOpacity),
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

    if (showCitySelector && !addedCities.isNullOrEmpty()) {
        WeatherCitySelector(
            addedCities = addedCities,
            currentCityData = currentCityData,
            appState = mainViewModel.appState(),
            onSwitchCity = { cityData ->
                mainViewModel.appState().setCurrentCityData(cityData)
            },
            onDismiss = { showCitySelector = false }
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