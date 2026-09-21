package com.yd.weather.weatherpreview

import android.app.Activity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.annotation.DrawableRes
import com.yd.weather.res.CommonIcon
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.yd.weather.R
import com.yd.weather.component.AppRow
import com.yd.weather.component.GlassIconButton
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.bounceClick
import com.yd.weather.navigation.AddCityResultKey
import com.yd.weather.utils.RefreshState
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
    // 列表内容作为背景来源，顶部玻璃和左右两个按钮共用同一份
    val backdrop = rememberLayerBackdrop()
    val refreshStateRef = remember { mutableStateOf<RefreshState?>(null) }
    var topBarOpacity by remember { mutableFloatStateOf(1f) }

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
                    // 跟天气首页一致：内容滚到状态栏底下，顶上盖渐进模糊，同时关掉卡片吸顶
                    glassTopBar = true,
                    backdrop = backdrop,
                    // 玻璃薄纱要取天气渐变的起点色，不传这里薄纱是全透明的，压不住底下的内容
                    weatherBg = weatherBg,
                    previewCity = true,
                    onRefresh = {
                        viewModel.refreshWeatherData { refreshStateRef.value?.refreshComplete() }
                    },
                    onRefreshState = { refreshStateRef.value = it },
                    onContentVisibilityChange = { show -> topBarOpacity = if (show) 1f else 0f }
                )
            }
            val animatedTopBarOpacity by animateFloatAsState(
                targetValue = topBarOpacity,
                animationSpec = tween(durationMillis = 200),
                label = "topBarOpacity"
            )
            AppRow(
                modifier = Modifier
                    .alpha(animatedTopBarOpacity)
                    .padding(start = 16.dp, top = statusBarTop + 12.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FunctionButton(
                    R.mipmap.ic_close_icon1,
                    backdrop = backdrop,
                    isWeatherHeaderDark = isWeatherHeaderDark,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                ) {
                    viewModel.navigateBack()
                }

                FunctionButton(
                    R.mipmap.ic_add,
                    backdrop = backdrop,
                    isWeatherHeaderDark = isWeatherHeaderDark,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                ) {
                    viewModel.popBackStackWithResult(AddCityResultKey, viewModel.cityId ?: "")
                }
            }
        }
    }
}

@Composable
fun FunctionButton(
    @DrawableRes iconRes: Int,
    backdrop: LayerBackdrop? = null,
    isWeatherHeaderDark: Boolean = false,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onClick: () -> Unit
) {
    // 有 backdrop 时跟天气首页右上角那颗按钮同一套：天气背景是深是浅，
    // 决定圆底压白还是压黑，图标也跟着反过来
    val icon: @Composable () -> Unit = {
        CommonIcon(
            resId = iconRes,
            size = 20.dp,
            tint = colorResource(
                if (backdrop == null || isWeatherHeaderDark) R.color.color_white
                else R.color.color_black
            ),
        )
    }

    if (backdrop != null) {
        GlassIconButton(
            backdrop = backdrop,
            // 正圆，直径与城市管理页顶栏按钮一致
            modifier = Modifier.bounceClick(onClick = onClick),
            tint = if (isWeatherHeaderDark) {
                Color.White.copy(alpha = 0.22f)
            } else {
                Color.Black.copy(alpha = 0.12f)
            },
            content = icon,
        )
        return
    }

    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .size(44.dp)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}