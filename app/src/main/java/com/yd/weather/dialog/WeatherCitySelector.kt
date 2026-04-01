package com.yd.weather.dialog

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.yd.weather.R
import com.yd.weather.app.AppState
import com.yd.weather.component.AppText
import com.yd.weather.component.bounceClick
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.rememberElasticScrollState
import com.yd.weather.widget.WeatherCitySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 天气城市选择器
 */
@SuppressLint("ConfigurationScreenWidthHeight", "FrequentlyChangingValue")
@Composable
fun WeatherCitySelector(
    addedCities: List<CityData>,
    currentCityData: CityData?,
    appState: AppState,
    onBlurChange: (Float) -> Unit = {},
    onSwitchCity: (cityData: CityData, isSameCity: Boolean) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToWeatherBgList: () -> Unit = {}
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

    // 背景模糊淡入/淡出
    val blurAlpha = remember { Animatable(0f) }
    // Swiper 滑入状态
    var isSwiperShow by remember { mutableStateOf(false) }
    // Swiper 透明度（参照 Flutter _opacity，选中时淡出）
    var swiperOpacity by remember { mutableFloatStateOf(1f) }
    // 选中后快照放大 0.6→1（参照 Flutter _scale）
    val scaleAnim = remember { Animatable(0.6f) }
    // 选中的城市 index（-1 表示未选中）
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val initialIndex = addedCities.indexOfFirst { it.cityId == currentCityData?.cityId }
        .coerceAtLeast(0)
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState)
    // 当前居中的 item index
    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }

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
    // 入场从 -screenWidth 滑入；退场需滑到 -(screenWidth + itemWidth) 确保最右侧 item 也完全离开视口
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val itemWidthDp = screenWidthDp * 0.6f
    val itemWidthPx = with(density) { itemWidthDp.dp.toPx() }
    val swiperTranslateX = remember { Animatable(-screenWidthPx) }

    // blurAlpha 变化时同步模糊值到外部（0~1 → 0~30dp）
    LaunchedEffect(blurAlpha.value) {
        onBlurChange(blurAlpha.value * 30f)
    }

    val view = LocalView.current

    // 入场动画（参照鸿蒙 aboutToAppear）
    LaunchedEffect(Unit) {
        delay(16)
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        launch { blurAlpha.animateTo(1f, tween(200)) }
        isSwiperShow = true
    }

    // isSwiperShow 驱动 Swiper 滑入
    LaunchedEffect(isSwiperShow) {
        if (isSwiperShow) {
            swiperTranslateX.animateTo(
                0f,
                spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
        }
    }

    // 关闭弹窗（参照 Flutter exit + _dismiss）
    val exit: () -> Unit = {
        scope.launch {
            // Swiper 滑出 + 背景淡出（同时进行，等两者都完成再关闭）
            kotlinx.coroutines.coroutineScope {
                // 背景稍晚淡出，避免背景先消失而卡片还在
                launch {
                    delay(100)
                    blurAlpha.animateTo(0f, tween(250, easing = FastOutSlowInEasing))
                }
                launch {
                    swiperTranslateX.animateTo(
                        -(screenWidthPx + itemWidthPx),
                        tween(300, easing = FastOutSlowInEasing)
                    )
                }
            }
            onDismiss()
        }
    }

    // 预测返回手势（参照 AirQualityDetailPopup）
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { backEvent ->
                val p = backEvent.progress
                // 手势进度：swiper 向左滑出 + 背景淡出
                swiperTranslateX.snapTo(-(screenWidthPx + itemWidthPx) * p)
                blurAlpha.snapTo(1f - p)
            }
            // 手势完成 → 快速收尾动画 + 关闭
            kotlinx.coroutines.coroutineScope {
                launch { swiperTranslateX.animateTo(-(screenWidthPx + itemWidthPx), tween(150)) }
                launch { blurAlpha.animateTo(0f, tween(150)) }
            }
            onDismiss()
        } catch (_: kotlin.coroutines.cancellation.CancellationException) {
            // 手势取消 → 弹回原位
            kotlinx.coroutines.coroutineScope {
                launch { swiperTranslateX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                launch { blurAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow)) }
            }
        }
    }

    // 切换城市（参照 Flutter _switchWeatherCity）
    val switchCity: (Int, Long) -> Unit = { index, delayMs ->
        scope.launch {
            // 1. 延迟（非居中 item 点击时等滚动完成）
            if (delayMs > 0) delay(delayMs)
            // 2. 显示快照（scale 0.6）
            selectedIndex = index
            // 3. 等 200ms 让快照渲染
            delay(200)
            // 4. 判断是否同一城市，回调切换（参照 Flutter eventBus.fire SwitchWeatherCityEvent）
            val isSame = snapshots[index].cityData.cityId == currentCityData?.cityId
            onSwitchCity(snapshots[index].cityData, isSame)
            // 5. Swiper 淡出 + 快照放大到全屏
            swiperOpacity = 0f
            scaleAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
            // 6. 放大完成后关闭
            onDismiss()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = exit
        )
    ) {
        // 半透明遮罩
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
                    .bounceClick(onClick = {
                        onDismiss()
                        onNavigateToWeatherBgList()
                    })
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

        // LazyRow + snap 卡片
        val animatedSwiperOpacity by animateFloatAsState(
            targetValue = swiperOpacity,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "swiperOpacity"
        )
        val horizontalPadding = ((screenWidthDp - itemWidthDp) / 2f).dp

        val elastic = rememberElasticScrollState(orientation = Orientation.Horizontal)
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeightDp * 0.6f).dp)
                .nestedScroll(elastic.connection)
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = animatedSwiperOpacity
                    translationX = elastic.overscrollOffset
                },
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = snapFlingBehavior
        ) {
            itemsIndexed(snapshots, key = { i, _ -> i }) { index, snapshot ->
                // 计算当前 item 相对于视口中心的偏移比例
                val itemOffset by remember {
                    derivedStateOf {
                        val layoutInfo = lazyListState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                            ?: return@derivedStateOf 1f
                        val viewportCenter = layoutInfo.viewportStartOffset +
                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                        val itemCenter = itemInfo.offset + itemInfo.size / 2f
                        abs(itemCenter - viewportCenter) / itemInfo.size.toFloat()
                    }
                }
                val itemScale = 1f - itemOffset.coerceIn(0f, 1f) * 0.1f

                Box(
                    modifier = Modifier
                        .width(itemWidthDp.dp)
                        .height((screenHeightDp * 0.6f).dp)
                        .graphicsLayer {
                            scaleX = itemScale
                            scaleY = itemScale
                            translationX = swiperTranslateX.value
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                if (currentIndex == index) {
                                    // 居中 item 点击 → 立即切换
                                    switchCity(index, 0L)
                                } else {
                                    // 非居中 item 点击 → 滚动过去 + 延迟后切换
                                    scope.launch {
                                        lazyListState.animateScrollToItem(index)
                                    }
                                    switchCity(index, 200L)
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
