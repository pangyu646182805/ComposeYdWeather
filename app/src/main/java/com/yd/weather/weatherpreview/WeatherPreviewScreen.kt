package com.yd.weather.weatherpreview

import android.app.Activity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.R
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.bounceClick
import com.yd.weather.navigation.AddCityResultKey
import com.yd.weather.utils.SetStatusBarStyle
import com.yd.weather.viewmodel.WeatherPreviewViewModel
import com.yd.weather.widget.WeatherContentList
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun WeatherPreviewRoute(
    viewModel: WeatherPreviewViewModel = hiltViewModel()
) {
    val statusBarTop = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    val viewState by viewModel.viewState.collectAsState()
    val weatherItems by viewModel.weatherItems.collectAsStateWithLifecycle()
    val itemTypeObserves by viewModel.itemTypeObserves.collectAsStateWithLifecycle()
    val weatherBg by viewModel.weatherBg.collectAsStateWithLifecycle()
    val isWeatherHeaderDark by viewModel.isWeatherHeaderDark.collectAsStateWithLifecycle()
    val isDark by viewModel.isDark.collectAsStateWithLifecycle()
    val panelOpacity by viewModel.panelOpacity.collectAsStateWithLifecycle()
    val currentCityData by viewModel.appState().currentCityData.collectAsStateWithLifecycle()

    SetStatusBarStyle(isLight = !isWeatherHeaderDark)

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

    val weatherScrollState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    isWeatherHeaderDark = isWeatherHeaderDark,
                    currentCityData = currentCityData,
                    weatherItems = weatherItems,
                    itemTypeObserves = itemTypeObserves,
                    showSortCardButton = false,
                    previewCity = true
                )
            }
            AppRow(
                modifier = Modifier
                    .padding(start = 12.dp, top = statusBarTop + 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FunctionButton("取消", isDark = isDark, panelOpacity = panelOpacity) {
                    viewModel.navigateBack()
                }

                FunctionButton("添加", isDark = isDark, panelOpacity = panelOpacity) {
                    viewModel.popBackStackWithResult(AddCityResultKey, viewModel.cityId ?: "")
                }
            }
        }
    }
}

@Composable
fun FunctionButton(
    text: String,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .height(32.dp)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = text,
            color = colorResource(R.color.color_white),
            fontSize = 14.sp,
        )
    }
}