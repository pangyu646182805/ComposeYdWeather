package com.yd.weather.dialog

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yd.weather.R
import com.yd.weather.app.AppState
import com.yd.weather.component.AppText
import com.yd.weather.component.bounceClick
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.widget.WeatherCitySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.core.graphics.createBitmap

/**
 * 天气城市选择器弹窗（参照鸿蒙 WeatherCitySelector）
 */
@SuppressLint("ConfigurationScreenWidthHeight", "FrequentlyChangingValue")
@Composable
fun WeatherCitySelector(
    addedCities: List<CityData>,
    currentCityData: CityData?,
    appState: AppState,
    onSwitchCity: (CityData) -> Unit,
    onDismiss: () -> Unit
) {
    // 空列表保护
    if (addedCities.isEmpty()) {
        onDismiss()
        return
    }

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // 截取当前页面作为模糊背景
    val view = LocalView.current
    val blurBitmap = remember { captureBlurBitmap(view) }

    // 背景模糊淡入/淡出
    val blurAlpha = remember { Animatable(0f) }
    // Swiper 滑入状态
    var isSwiperShow by remember { mutableStateOf(false) }
    // Swiper 透明度（参照 Flutter _opacity，选中时淡出）
    var swiperOpacity by remember { mutableStateOf(1f) }
    // 选中后快照放大 0.6→1（参照 Flutter _scale）
    val scaleAnim = remember { Animatable(0.6f) }
    // 选中的城市 index（-1 表示未选中）
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val initialIndex = addedCities.indexOfFirst { it.cityId == currentCityData?.cityId }
        .coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex) { addedCities.size }

    // 为每个城市生成快照数据
    val snapshots = remember(addedCities) {
        addedCities.map { city ->
            val key = if (city.isLocationCity) Constants.LOCATION_CITY_ID else city.cityId ?: ""
            val weatherData = appState.getWeatherData(key)
            val weatherBg = appState.generateWeatherBg(
                weatherData, city.weatherData?.weatherType,
                city.weatherData?.sunrise, city.weatherData?.sunset
            )
            val itemTypeObserves = appState.getItemTypeObserves(
                appState.currentWeatherObservesCardSort.value,
                Constants.ITEM_TYPE_OBSERVE, weatherData
            )
            SnapshotData(
                cityData = city,
                weatherBg = weatherBg,
                isDark = appState.isDark(weatherBg),
                isWeatherHeaderDark = appState.isWeatherHeaderDark(weatherBg),
                panelOpacity = appState.calPanelOpacity(weatherBg),
                itemTypeObserves = itemTypeObserves,
                weatherItems = appState.generateWeatherItems(itemTypeObserves, weatherData)
            )
        }
    }

    // Swiper 水平位移动画（参照鸿蒙 translate + interpolatingSpring）
    val swiperTranslateX = remember { Animatable(with(density) { -screenWidthDp.dp.toPx() }) }

    // 入场动画（参照鸿蒙 aboutToAppear）
    LaunchedEffect(Unit) {
        delay(16)
        launch { blurAlpha.animateTo(1f, tween(200)) }
        isSwiperShow = true
    }

    // isSwiperShow 驱动 Swiper 滑入/滑出
    LaunchedEffect(isSwiperShow) {
        if (isSwiperShow) {
            swiperTranslateX.animateTo(
                0f,
                spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
        } else {
            swiperTranslateX.animateTo(
                with(density) { -screenWidthDp.dp.toPx() },
                tween(200)
            )
        }
    }

    // 关闭弹窗（参照 Flutter exit + _dismiss）
    val exit: () -> Unit = {
        scope.launch {
            // Swiper 滑出 + 背景淡出
            isSwiperShow = false
            launch { blurAlpha.animateTo(0f, tween(200)) }
            delay(200)
            onDismiss()
        }
    }

    // 切换城市（参照 Flutter _switchWeatherCity）
    val switchCity: (Int) -> Unit = { index ->
        scope.launch {
            // 1. 显示快照（scale 0.6）
            selectedIndex = index
            // 2. 等 200ms 让快照渲染
            delay(200)
            // 3. 切换城市数据
            onSwitchCity(snapshots[index].cityData)
            // 4. 同时：Swiper 淡出 + 快照放大到全屏
            swiperOpacity = 0f
            scaleAnim.animateTo(1f, tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            // 5. 放大完成后关闭
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = exit,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // 去掉 Dialog dim
        val dialogView = LocalView.current
        LaunchedEffect(dialogView) {
            try {
                val rootView = dialogView.rootView
                (rootView.layoutParams as? android.view.WindowManager.LayoutParams)?.let { lp ->
                    lp.dimAmount = 0f
                    lp.flags = lp.flags or android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    val wm = rootView.context.getSystemService(
                        android.content.Context.WINDOW_SERVICE
                    ) as android.view.WindowManager
                    wm.updateViewLayout(rootView, lp)
                }
            } catch (_: Exception) {
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // 模糊背景（参照鸿蒙 blur(blurAnimValue * 100)）
            if (blurBitmap != null) {
                Image(
                    bitmap = blurBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(blurAlpha.value)
                        .graphicsLayer {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                renderEffect = RenderEffect
                                    .createBlurEffect(80f, 80f, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                        }
                )
            }

            // 半透明蒙层（参照鸿蒙 backgroundColor alpha 0.15）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha.value)
                    .background(Color.Black.copy(alpha = 0.15f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = exit
                    )
            )

            // "更改天气背景"按钮（参照鸿蒙 changeWeatherBgButton）
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .alpha(blurAlpha.value)
            ) {
                AppText(
                    modifier = Modifier
                        .bounceClick(onClick = exit)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            RoundedCornerShape(100.dp)
                        )
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = 0.5f),
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    text = "更改天气背景",
                    fontSize = 16.sp,
                    color = colorResource(R.color.color_white)
                )
            }

            // Swiper 卡片（参照 Flutter ScrollSnapList + AnimatedOpacity）
            val animatedSwiperOpacity by androidx.compose.animation.core.animateFloatAsState(
                targetValue = swiperOpacity,
                animationSpec = tween(200),
                label = "swiperOpacity"
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeightDp * 0.6f).dp)
                    .align(Alignment.Center)
                    .alpha(animatedSwiperOpacity)
                    .graphicsLayer { translationX = swiperTranslateX.value },
                    contentPadding = PaddingValues(horizontal = (screenWidthDp * 0.2f).dp),
                    pageSpacing = 12.dp,
                    beyondViewportPageCount = 1
                ) { page ->
                    val snapshot = snapshots.getOrNull(page) ?: return@HorizontalPager
                    // 缩放：当前页 1.0，相邻页 0.9（参照鸿蒙 customContentTransition）
                    val pageOffset = abs(
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    )
                    val itemScale = 1f - pageOffset.coerceIn(0f, 1f) * 0.1f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if (pagerState.currentPage == page) {
                                        switchCity(page)
                                    } else {
                                        scope.launch { pagerState.animateScrollToPage(page) }
                                    }
                                }
                            )
                    ) {
                        WeatherCitySnapshot(
                            cityData = snapshot.cityData,
                            weatherItems = snapshot.weatherItems,
                            weatherBg = snapshot.weatherBg,
                            isDark = snapshot.isDark,
                            isWeatherHeaderDark = snapshot.isWeatherHeaderDark,
                            panelOpacity = snapshot.panelOpacity,
                            itemTypeObserves = snapshot.itemTypeObserves,
                            scale = 0.6f
                        )
                    }
                }

            // 选中后全屏放大快照（参照 Flutter AnimatedScale 0.6→1 + onEnd → dismiss）
            if (selectedIndex in snapshots.indices) {
                val snapshot = snapshots[selectedIndex]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    WeatherCitySnapshot(
                        cityData = snapshot.cityData,
                        weatherItems = snapshot.weatherItems,
                        weatherBg = snapshot.weatherBg,
                        isDark = snapshot.isDark,
                        isWeatherHeaderDark = snapshot.isWeatherHeaderDark,
                        panelOpacity = snapshot.panelOpacity,
                        itemTypeObserves = snapshot.itemTypeObserves,
                        scale = scaleAnim.value
                    )
                }
            }
        }
    }
}

/** 截取当前 View 的 Bitmap 用于模糊背景 */
private fun captureBlurBitmap(view: View): Bitmap? {
    return try {
        if (view.width <= 0 || view.height <= 0) return null
        val bitmap = createBitmap(view.width, view.height)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        bitmap
    } catch (_: Exception) {
        null
    }
}

private data class SnapshotData(
    val cityData: CityData,
    val weatherBg: List<Color>,
    val isDark: Boolean,
    val isWeatherHeaderDark: Boolean,
    val panelOpacity: Float,
    val itemTypeObserves: Array<Int>?,
    val weatherItems: List<WeatherItemData>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnapshotData

        if (isDark != other.isDark) return false
        if (isWeatherHeaderDark != other.isWeatherHeaderDark) return false
        if (panelOpacity != other.panelOpacity) return false
        if (cityData != other.cityData) return false
        if (weatherBg != other.weatherBg) return false
        if (!itemTypeObserves.contentEquals(other.itemTypeObserves)) return false
        if (weatherItems != other.weatherItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isDark.hashCode()
        result = 31 * result + isWeatherHeaderDark.hashCode()
        result = 31 * result + panelOpacity.hashCode()
        result = 31 * result + cityData.hashCode()
        result = 31 * result + weatherBg.hashCode()
        result = 31 * result + (itemTypeObserves?.contentHashCode() ?: 0)
        result = 31 * result + (weatherItems?.hashCode() ?: 0)
        return result
    }
}
