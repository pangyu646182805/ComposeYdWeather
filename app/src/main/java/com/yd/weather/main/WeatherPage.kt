package com.yd.weather.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.yd.weather.db.model.CityData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.WeatherContentClip
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel

@Composable
fun WeatherPage(
    viewState: ViewState = ViewState.Loading,
    cityManagerScrollState: LazyListState = rememberLazyListState(),
    isShowWeatherPage: Boolean = true,
    addedCities: List<CityData>? = null,
    weatherBg: List<Color> = emptyList(),
    isWeatherHeaderDark: Boolean = false,
    isDark: Boolean = false,
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
    if (animValue < 1) {
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
                MultipleStatusView(viewState = viewState) {
                    WeatherContentList(
                        weatherScrollState = weatherScrollState,
                        isShowWeatherPage = isShowWeatherPage,
                        animValue = animValue
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
    animValue: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(
                if (isShowWeatherPage) 1 - ((animValue - 0.8f) / 0.2f).coerceIn(
                    0f, 1f
                ) else ((0.2f - animValue) / 0.2f).coerceIn(0f, 1f)
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = weatherScrollState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(20) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(98.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }
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