package com.yd.weather.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.yd.weather.component.LiquidGlassFadeHeight
import com.yd.weather.component.LiquidGlassScrim
import com.yd.weather.component.VerticalSpace
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
    /** 顶部改用液态玻璃：内容一路延伸到状态栏底下，顶上盖一层渐进模糊 */
    glassTopBar: Boolean = false,
    /** 外部传入的 backdrop，让顶栏按钮之类页面级元素也能取到同一份背景 */
    backdrop: LayerBackdrop? = null,
    onCardSortButtonClick: () -> Unit = {},
    weatherBg: List<Color> = emptyList(),
    previewCity: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onRefreshState: ((RefreshState) -> Unit)? = null,
    onContentVisibilityChange: ((Boolean) -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    var contentOpacity by remember { mutableFloatStateOf(1f) }
    val showHideWeatherContent: (Boolean) -> Unit = { show ->
        contentOpacity = if (show) 1f else 0f
        onContentVisibilityChange?.invoke(show)
    }

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

    // 顶部玻璃取用的背景来源，只有 glassTopBar 时才真正挂上去
    val ownBackdrop = rememberLayerBackdrop()
    val contentBackdrop = backdrop ?: ownBackdrop
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // 玻璃罩住的是大标题收缩到最小之后占的那一段
    val glassHeight = statusBarTop + Constants.WEATHER_HEADER_MIN_HEIGHT.dp

    val firstItemOffset by remember {
        derivedStateOf { with(density) { weatherScrollState.firstVisibleItemScrollOffset.toDp().value } }
    }
    val firstVisibleItemIndex by remember {
        derivedStateOf { weatherScrollState.firstVisibleItemIndex }
    }

    // 玻璃模式下关掉卡片的吸顶：内容要整条滚到顶栏背后去，
    // 吸顶标题会停在被玻璃盖住的那片区域里，隔着模糊仍然看得见，很脏。
    // WeatherStickyPanel 的 offset 在 index + 1 > firstVisibleItemIndex 时恒为 0，
    // 把这两个值钉死就等于让每张卡片一直处在"还没滚到顶"的常态。
    // 大标题的收缩另算，它用的仍是真实的 firstItemOffset。
    val panelItemOffset = if (glassTopBar) 0f else firstItemOffset
    val panelVisibleIndex = if (glassTopBar) 0 else firstVisibleItemIndex

    val animatedContentOpacity by animateFloatAsState(
        targetValue = contentOpacity,
        animationSpec = tween(durationMillis = 200),
        label = "contentOpacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(
                animatedContentOpacity * (if (isShowWeatherPage)
                    1 - ((animValue - 0.8f) / 0.2f).coerceIn(0f, 1f)
                else
                    ((0.2f - animValue) / 0.2f).coerceIn(0f, 1f))
            )
    ) {
        Box(
            modifier = Modifier
                // 玻璃模式下这两段垂直留白全部并进列表第一项，列表本身顶到屏幕顶，
                // 内容才能滚到状态栏底下去
                .then(if (glassTopBar) Modifier else Modifier.statusBarsPadding())
                .graphicsLayer { translationY = refreshOffset }
                .padding(
                    top = if (glassTopBar) 0.dp else Constants.WEATHER_HEADER_MIN_HEIGHT.dp,
                    start = Constants.ITEM_PANEL_MARGIN.dp,
                    end = Constants.ITEM_PANEL_MARGIN.dp
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(refreshState.connection)
                    .then(
                        // 玻璃模式下内容要滚到顶栏背后，再裁圆角就成了一刀切
                        if (glassTopBar) Modifier.layerBackdrop(contentBackdrop)
                        else Modifier.clip(
                            RoundedCornerShape(
                                topStart = Constants.ITEM_PANEL_RADIUS.dp,
                                topEnd = Constants.ITEM_PANEL_RADIUS.dp
                            )
                        )
                    ),
                state = weatherScrollState
            ) {
                item {
                    // 不用 contentPadding：firstVisibleItemScrollOffset 要等 contentPadding
                    // 滚完才开始涨，大标题的收缩曲线会整体延迟一个顶栏的高度才启动。
                    // 并进第一项的高度里，firstItemOffset 照旧从 0 开始。
                    VerticalSpace(
                        height = if (glassTopBar) {
                            statusBarTop + Constants.WEATHER_HEADER_MAX_HEIGHT.dp
                        } else {
                            (Constants.WEATHER_HEADER_MAX_HEIGHT - Constants.WEATHER_HEADER_MIN_HEIGHT).dp
                        }
                    )
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
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex,
                                showHideWeatherContent = showHideWeatherContent,
                                onLongPress = onLongPress
                            )

                            Constants.ITEM_TYPE_AIR_QUALITY -> WeatherAirQualityPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex,
                                showHideWeatherContent = showHideWeatherContent,
                                onLongPress = onLongPress
                            )

                            Constants.ITEM_TYPE_HOUR_WEATHER -> WeatherHourPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex
                            )

                            Constants.ITEM_TYPE_DAILY_WEATHER -> WeatherDailyPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex,
                                weatherBg = weatherBg,
                                showHideWeatherContent = showHideWeatherContent,
                                onLongPress = onLongPress
                            )

                            Constants.ITEM_TYPE_OBSERVE -> WeatherObservePanel(
                                item = item,
                                itemTypeObserves = itemTypeObserves,
                                index = index,
                                isDark = isDark,
                                isWeatherHeaderDark = isWeatherHeaderDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex,
                                showHideWeatherContent = showHideWeatherContent,
                                onLongPress = onLongPress
                            )

                            Constants.ITEM_TYPE_LIFE_INDEX -> WeatherLifeIndexPanel(
                                item = item,
                                index = index,
                                isDark = isDark,
                                panelOpacity = panelOpacity,
                                firstItemOffset = panelItemOffset,
                                firstVisibleItemIndex = panelVisibleIndex
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
                            showSortCardButton = showSortCardButton,
                            onCardSortButtonClick = onCardSortButtonClick
                        )
                    }
                }
            }
        }
        if (glassTopBar) {
            // 薄纱用天气背景自己的渐变起点色 —— 这页底色是整片渐变，
            // 铺主题背景色（白/黑）会在顶部糊出一条和页面不搭的色带。
            // 浓度不能太低：只靠模糊压不住大字号内容，城市名背后会透出可辨认的文字
            val scrimBase = weatherBg.firstOrNull() ?: Color.Transparent
            LiquidGlassScrim(
                backdrop = contentBackdrop,
                height = glassHeight + LiquidGlassFadeHeight,
                scrimColor = if (isSystemInDarkTheme()) {
                    // 深色模式下天气页在渐变之上还叠了一层黑（顶部 0.25、底部 0.15），
                    // 薄纱不跟着压暗的话，顶部会亮出一块比页面浅的色带
                    Color.Black.copy(alpha = 0.25f).compositeOver(scrimBase)
                } else {
                    scrimBase
                },
                scrimAlpha = 0.9f,
            )
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