package com.yd.weather.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.graphicsLayer
import com.yd.weather.utils.weatherCardTop
import com.yd.weather.utils.weatherContentAlpha
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.yd.weather.component.CenterTopAppBar
import com.yd.weather.component.GlassIconButton
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.db.model.CityData
import com.yd.weather.dialog.WeatherCitySelector
import com.yd.weather.model.WeatherItemData
import com.yd.weather.routes.CardSortRoutes
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.RefreshState
import com.yd.weather.utils.WeatherContentClip
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel
import com.yd.weather.widget.WeatherContentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.abs

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
    val scope = rememberCoroutineScope()
    val weatherScrollState = rememberLazyListState()
    // 列表内容作为背景来源，顶部玻璃和右上角按钮共用同一份
    val backdrop = rememberLayerBackdrop()
    // 保存 refreshState 引用，用于数据加载完成后调用 refreshComplete()
    val refreshStateRef = remember { mutableStateOf<RefreshState?>(null) }
    var topBarOpacity by remember { mutableFloatStateOf(1f) }
    var showCitySelector by remember { mutableStateOf(false) }
    var citySelectorBlur by remember { mutableFloatStateOf(0f) }
    val animatable = remember { Animatable(if (isShowWeatherPage) 0f else 1f) }
    // 卡片落回列表之后还要再淡出一下，露出底下已经就位的那张城市卡片
    val cardAlpha = remember { Animatable(if (isShowWeatherPage) 1f else 0f) }
    val predictiveBackProgress by mainViewModel.predictiveBackProgress.collectAsStateWithLifecycle()

    // 手势进行中 - snap 跟随手势进度
    LaunchedEffect(predictiveBackProgress) {
        val p = predictiveBackProgress ?: return@LaunchedEffect
        // 手势一开始就跟点击展开一样：其余城市立刻藏掉，卡片变成不透明的空卡片跟着手指长大
        cityManagerViewModel.hideCityList()
        cardAlpha.snapTo(1f)
        animatable.snapTo((1f - p).coerceIn(0f, 1f))
    }

    // isShowWeatherPage 变化或手势结束 - 过渡到目标值
    LaunchedEffect(isShowWeatherPage, predictiveBackProgress == null) {
        if (predictiveBackProgress != null) return@LaunchedEffect
        if (isShowWeatherPage) {
            // 展开：其余城市第一帧就藏掉，卡片在干净的底上长大
            cityManagerViewModel.hideCityList()
            cardAlpha.snapTo(1f)
            if (animatable.value != 0f) {
                animatable.animateTo(0f, cardTransitionSpec(from = animatable.value, to = 0f))
            }
        } else {
            // 收回：卡片还没落地，城市列表就先回来（这时仍被卡片盖着，看不见），
            // 落地后卡片再淡出，露出来的正好是那张城市卡片。
            // 以前是等动画彻底停下才显示列表，卡片先淡没、列表后冒出来，中间会空一帧
            var listShown = false
            if (animatable.value != 1f) {
                animatable.animateTo(1f, cardTransitionSpec(from = animatable.value, to = 1f)) {
                    if (!listShown && value >= LIST_REVEAL_PROGRESS) {
                        listShown = true
                        cityManagerViewModel.showCityList()
                    }
                }
            }
            if (!listShown) cityManagerViewModel.showCityList()
            if (cardAlpha.value > 0f) {
                cardAlpha.animateTo(0f, tween(durationMillis = CARD_LANDING_FADE_MILLIS))
            }
        }
    }

    val animValue = animatable.value
    if (animValue < 1f || cardAlpha.value > 0f) {
        // 已经落回列表、只剩淡出那一小段：只画空卡片，内容和手势都不要，
        // 否则这一层全屏的天气页会挡在城市列表上面，点不动
        val cardOnly = animValue >= 1f
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
                .then(
                    if (cardOnly) Modifier
                    else Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showCitySelector = true
                            }
                        )
                    }
                )
                .graphicsLayer {
                    // 整个天气页钉在卡片上边缘跟着走，城市名和大温度始终在卡片左上角领头
                    translationY = weatherCardTop(animValue, mainViewModel.offsetY)
                    alpha = cardAlpha.value
                }
                .clip(WeatherContentClip(animValue))
                .then(if (citySelectorBlur > 0f) Modifier.blur(citySelectorBlur.dp) else Modifier)
                .background(
                    brush = Brush.verticalGradient(colors = listOf(startColor, endColor))
                )
        ) {
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
            if (!cardOnly) {
                val contentAlpha = weatherContentAlpha(animValue)
                MultipleStatusView(
                    viewState = viewState,
                    // 加载中、出错也一起淡；不然空卡片长大的前半程会先冒出一个转圈
                    modifier = Modifier.alpha(contentAlpha),
                    loadingColor = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                ) {
                    WeatherContentList(
                        weatherScrollState = weatherScrollState,
                        glassTopBar = true,
                        backdrop = backdrop,
                        isDark = isDark,
                        panelOpacity = panelOpacity,
                        isWeatherHeaderDark = isWeatherHeaderDark,
                        weatherBg = weatherBg,
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
                        },
                        onLongPress = { showCitySelector = true }
                    )
                }
                val animatedTopBarOpacity by animateFloatAsState(
                    targetValue = topBarOpacity,
                    animationSpec = tween(durationMillis = 200),
                    label = "topBarOpacity"
                )
                CenterTopAppBar(
                    // 右上角按钮也是天气页的内容，跟着一起淡；不然会被卡片带着满屏跑
                    modifier = Modifier.alpha(animatedTopBarOpacity * contentAlpha),
                    showBackIcon = false,
                    colors = topAppBarColors(containerColor = colorResource(R.color.transparent)),
                    actions = {
                        RightIcon(
                            backdrop = backdrop,
                            isWeatherHeaderDark = isWeatherHeaderDark,
                        ) {
                            mainViewModel.showCityManagerPage(
                                cityManagerViewModel, cityManagerScrollState
                            )
                        }
                    },
                )
            }
        }
    }

    // 城市选择器 — 在 clipped Box 外部，不受裁剪影响
    if (showCitySelector && !addedCities.isNullOrEmpty()) {
        WeatherCitySelector(
            addedCities = addedCities,
            currentCityData = currentCityData,
            appState = mainViewModel.appState(),
            onBlurChange = { citySelectorBlur = it },
            onSwitchCity = { cityData, isSameCity ->
                // 参照 Flutter _switchWeatherCityEventSubscription
                // 延迟 200ms 后 scrollToTop
                scope.launch {
                    delay(200)
                    weatherScrollState.animateScrollToItem(0)
                }
                // 不同城市才切换数据（会自动触发 obtainWeatherData）
                if (!isSameCity) {
                    mainViewModel.appState().setCurrentCityData(cityData)
                }
            },
            onDismiss = {
                showCitySelector = false
                citySelectorBlur = 0f
            },
            onNavigateToWeatherBgList = {
                mainViewModel.navigate(com.yd.weather.routes.WeatherBgRoutes.WeatherBgList)
            }
        )
    }
}

@Composable
fun RightIcon(
    backdrop: LayerBackdrop? = null,
    isWeatherHeaderDark: Boolean = false,
    onClick: () -> Unit
) {
    // 天气背景是深是浅，决定圆底压白还是压黑
    val tint = if (isWeatherHeaderDark) {
        Color.White.copy(alpha = 0.22f)
    } else {
        Color.Black.copy(alpha = 0.12f)
    }
    val icon: @Composable () -> Unit = {
        CommonIcon(
            resId = R.mipmap.ic_add,
            size = 20.dp,
            tint = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
        )
    }

    IconButton(
        onClick = onClick,
        // M3 TopAppBar 的 actions 只留 4dp 边距，按钮会比城市管理页那两个更贴边。
        // 补到 10dp，圆底右缘正好落在与列表卡片一致的 16dp 上。
        modifier = Modifier.padding(end = 10.dp),
    ) {
        if (backdrop != null) {
            // 列表内容会从按钮底下滚过去，折射得到的是真实内容，与城市管理页一致
            GlassIconButton(backdrop = backdrop, tint = tint, content = icon)
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = tint, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) { icon() }
        }
    }
}

/** 卡片从列表项走到全屏（或反过来）走完全程的时长 */
private const val CARD_TRANSITION_MILLIS = 300

/** 卡片落回列表后，空卡片淡出、露出城市卡片的时长 */
private const val CARD_LANDING_FADE_MILLIS = 150

/** 收回走到这个进度时让城市列表回来，此时卡片还比列表项大一圈，正好盖住它 */
private const val LIST_REVEAL_PROGRESS = 0.78f

/**
 * 一镜到底的运动曲线：CSS 的 ease。
 * 拿小米天气录屏逐帧量出的卡片轨迹去拟合，它误差最小，展开和收回都是它
 */
private val CardTransitionEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/** 手势松手时剩下的不一定是全程，时长按剩余距离缩短，免得最后一小段慢吞吞的 */
private fun cardTransitionSpec(from: Float, to: Float): AnimationSpec<Float> = tween(
    durationMillis = (CARD_TRANSITION_MILLIS * abs(to - from)).roundToInt().coerceAtLeast(120),
    easing = CardTransitionEasing,
)
