package com.yd.weather.main

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drake.logcat.LogCat
import com.yd.weather.R
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.yd.weather.app.AppState
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppScaffold
import com.yd.weather.component.LiquidGlassScrim
import com.yd.weather.component.LiquidGlassTopBar
import com.yd.weather.component.liquidGlassTopBarHeight
import com.yd.weather.component.AppText
import com.yd.weather.component.StartAlignColumn
import com.yd.weather.component.SwipeRevealLayout
import com.yd.weather.component.SwipeRevealState
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.component.bounceClick
import com.yd.weather.component.rememberSwipeRevealState
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.Commons
import com.yd.weather.utils.ObserveListAddition
import com.yd.weather.utils.getToday
import com.yd.weather.utils.rememberElasticScrollState
import com.yd.weather.viewmodel.CityManagerViewModel
import com.yd.weather.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@Composable
fun CityManagerPage(
    isShowWeatherPage: Boolean = true,
    addedCities: List<CityData>? = null,
    scrollState: LazyListState = rememberLazyListState(),
    mainViewModel: MainViewModel = hiltViewModel(),
    viewModel: CityManagerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedList by viewModel.selectedList.collectAsStateWithLifecycle()
    val deleteButtonEnable by viewModel.deleteButtonEnable.collectAsStateWithLifecycle()
    val itemAlpha by viewModel.itemAlpha.collectAsStateWithLifecycle()
    // 收回时城市卡片的涟漪（见 cityRippleScale）。
    // 列表藏着的时候就把它拨回起点（反正看不见）：列表一出现，第一帧就已经是缩小的样子。
    // 要是等列表出现了再拨回去，LaunchedEffect 比重组晚一帧，会先闪一下原尺寸再缩回去。
    // 重新进入组合时（比如从别的页面返回）列表本来就显示着，直接给原尺寸，不播
    val rippleElapsed = remember { Animatable(if (itemAlpha > 0f) CITY_RIPPLE_DURATION_MS else 0f) }
    LaunchedEffect(itemAlpha) {
        if (itemAlpha > 0f) {
            val remaining = CITY_RIPPLE_DURATION_MS - rippleElapsed.value
            if (remaining > 0f) {
                rippleElapsed.animateTo(
                    CITY_RIPPLE_DURATION_MS,
                    tween(durationMillis = remaining.toInt(), easing = LinearEasing)
                )
            }
        } else {
            rippleElapsed.snapTo(0f)
        }
    }
    // 波从卡片落地的那张（当前城市）往两边扩散
    val currentCityData by viewModel.appState().currentCityData.collectAsStateWithLifecycle()
    val rippleOrigin = addedCities?.indexOfFirst { it.cityId == currentCityData?.cityId } ?: -1
    val density = LocalDensity.current

    ObserveListAddition(addedCities) {
        scope.launch {
            scrollState.animateScrollToItem(addedCities?.size ?: 0)
        }
    }

    val stickyHeaderHeightPx = with(density) { 60.dp.toPx() }

    val centerTitleAlpha by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else (scrollState.firstVisibleItemScrollOffset / stickyHeaderHeightPx).coerceIn(0f, 1f)
        }
    }

    val headerAlpha by remember {
        derivedStateOf { 1f - centerTitleAlpha }
    }

    val title =
        if (isEditMode) if (selectedList.isEmpty()) "请选择项目" else "已选择${selectedList.size}项" else "城市管理"

    BackHandler(enabled = isEditMode) {
        viewModel.closeEditMode()
    }

    val fullyVisibleIndices by remember {
        derivedStateOf {
            val layoutInfo = scrollState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (visibleItemsInfo.isEmpty()) {
                emptyList()
            } else {
                visibleItemsInfo
                    .filter { itemInfo ->
                        val itemStart = itemInfo.offset
                        val itemEnd = itemInfo.offset + itemInfo.size
                        val viewportStart = layoutInfo.viewportStartOffset
                        val viewportEnd = layoutInfo.viewportEndOffset
                        // 判断是否完全可见
                        itemStart >= viewportStart && itemEnd <= viewportEnd
                    }
                    .map { it.index }
            }
        }
    }

    LaunchedEffect(fullyVisibleIndices) {
        viewModel.fullyVisibleIndices = fullyVisibleIndices
    }

    PredictiveBackHandler(enabled = !isEditMode && !isShowWeatherPage) { progress ->
        try {
            progress.collect { backEvent ->
                mainViewModel.updatePredictiveBackProgress(backEvent.progress)
            }
            // 手势完成 - 返回天气页
            mainViewModel.updatePredictiveBackProgress(null)
            mainViewModel.showWeatherPage(viewModel, scrollState)
        } catch (e: CancellationException) {
            // 手势取消 - 恢复城市管理页
            mainViewModel.updatePredictiveBackProgress(null)
        }
    }

    // 顶栏改为浮在列表之上的玻璃层，backdrop 是它取用的背景来源
    val backdrop = rememberLayerBackdrop()
    val topBarHeightPx = with(density) { liquidGlassTopBarHeight().toPx() }

    AppScaffold(
        // 顶栏不再占布局空间，交给下面的 LiquidGlassTopBar 叠在内容上
        topBar = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    // 顶栏改成浮层后这个 Box 从屏幕顶起算，但 LazyListItemInfo.offset
                    // 是不含 contentPadding 的。两处都要把顶栏留白补回来，
                    // 否则一镜到底的目标位置会整体上移一个顶栏的高度。
                    viewModel.listOffsetY = it.positionOnScreen().y + topBarHeightPx
                }
                .graphicsLayer(clip = true), contentAlignment = Alignment.BottomCenter
        ) {
            CityList(
                // 声明这一层是玻璃顶栏取用的背景源
                modifier = Modifier.layerBackdrop(backdrop),
                addedCities = addedCities,
                appState = viewModel.appState(),
                isEditMode = isEditMode,
                isSelected = { cityData ->
                    viewModel.isSelected(cityData)
                },
                scrollState = scrollState,
                headerAlpha = headerAlpha,
                headerTitle = title,
                swap = mainViewModel::swapAddedCityData,
                onSwapDragStopped = mainViewModel::onSwapDragStopped,
                toEditMode = { cityData ->
                    viewModel.toEditMode(addedCities?.size ?: 0, cityData)
                },
                removeItem = { cityData ->
                    cityData ?: return@CityList
                    mainViewModel.removeCityData(cityData) {
                        viewModel.refreshCurrentCityIdList(listOf(cityData))
                        viewModel.afterRemove(
                            cityData.cityId == viewModel.appState().currentCityData.value?.cityId,
                            addedCities
                        )
                    }
                },
                onItemClick = { cityData ->
                    if (cityData == null) return@CityList
                    if (isEditMode) {
                        viewModel.selected(cityData)
                    } else {
                        val appState = viewModel.appState()
                        if (cityData.cityId != appState.currentCityData.value?.cityId) {
                            appState.setCurrentCityData(cityData)
                        }
                        mainViewModel.showWeatherPage(viewModel, scrollState)
                    }
                },
                changeDeleteButtonEnable = { enable ->
                    viewModel.changeDeleteButtonEnable(enable)
                },
                itemAlpha = itemAlpha,
                rippleOriginIndex = rippleOrigin,
                // 传函数而不是值：缩放在绘制阶段读，播放涟漪时不会让整个列表每帧重组
                rippleElapsedMs = { rippleElapsed.value }
            )
            // 必须在 CityList 之后、且不被 layerBackdrop 包住，否则会自己模糊自己
            LiquidGlassTopBar(
                backdrop = backdrop,
                title = title,
                titleAlpha = centerTitleAlpha,
                modifier = Modifier.align(Alignment.TopCenter),
                navigationIcon = {
                    LeftIcon(isEditMode = isEditMode) {
                        if (isEditMode) {
                            viewModel.closeEditMode()
                        } else {
                            mainViewModel.showWeatherPage(viewModel, scrollState)
                        }
                    }
                },
                actionIcon = {
                    RightIcon(
                        isEditMode = isEditMode,
                        isSelectedAll = viewModel.isSelectedAll(addedCities)
                    ) {
                        if (isEditMode) {
                            if (viewModel.isSelectedAll(addedCities)) {
                                viewModel.clearSelected()
                            } else {
                                viewModel.selectedAll(addedCities)
                            }
                        } else {
                            viewModel.toSelectCityPage()
                        }
                    }
                }
            )
            BottomOperateButton(
                backdrop = backdrop,
                isEditMode = isEditMode,
                hasSelected = viewModel.hasSelected(),
                deleteButtonEnable = deleteButtonEnable,
                removeItems = {
                    mainViewModel.removeCities(selectedList.toList()) {
                        val appState = viewModel.appState()
                        val resetCurrentCityData =
                            selectedList.find { removeItem -> removeItem.cityId == appState.currentCityData.value?.cityId } != null
                        viewModel.refreshCurrentCityIdList(selectedList)
                        viewModel.closeEditMode()
                        viewModel.afterRemove(resetCurrentCityData, addedCities)
                    }
                }
            )
        }
    }
}

@Composable
fun LeftIcon(isEditMode: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        CommonIcon(
            resId = if (isEditMode) R.mipmap.ic_close_icon1 else R.mipmap.ic_close_icon,
            size = 22.dp,
            tint = colorResource(R.color.black),
        )
    }
}

@Composable
fun RightIcon(isEditMode: Boolean = false, isSelectedAll: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        CommonIcon(
            resId = if (isEditMode) R.mipmap.ic_select_all_icon else R.mipmap.ic_search_icon,
            size = 20.dp,
            tint = colorResource(if (isEditMode && isSelectedAll) R.color.app_main else R.color.black),
        )
    }
}

@Composable
fun CityList(
    modifier: Modifier = Modifier,
    addedCities: List<CityData>? = null,
    appState: AppState,
    isEditMode: Boolean = false,
    isSelected: (cityData: CityData?) -> Boolean = { false },
    scrollState: LazyListState,
    headerAlpha: Float = 1f,
    headerTitle: String = "",
    swap: (fromIndex: Int, toIndex: Int) -> Unit,
    onSwapDragStopped: () -> Unit = {},
    toEditMode: (cityData: CityData?) -> Unit,
    removeItem: (cityData: CityData?) -> Unit,
    onItemClick: (cityData: CityData?) -> Unit,
    changeDeleteButtonEnable: (enable: Boolean) -> Unit = {},
    itemAlpha: Float = 0f,
    rippleOriginIndex: Int = -1,
    rippleElapsedMs: () -> Float = { CITY_RIPPLE_DURATION_MS }
) {
    // 当前正在拖拽的 item index，null 表示无 item 在拖拽
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    var openedItemKey by remember { mutableStateOf<String?>(null) }
    val elastic = rememberElasticScrollState()

    val reorderableLazyListState = rememberReorderableLazyListState(scrollState) { from, to ->
        // Update the list
        LogCat.e("reorderableLazyListState: ${from.index} -> ${to.index}")
        swap(from.index - 1, to.index - 1)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) openedItemKey = null
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(elastic.connection)
            .graphicsLayer { translationY = elastic.overscrollOffset },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            // 顶栏现在浮在内容之上，这里补出等高的留白，内容起始位置不变但能滚到它背后
            top = liquidGlassTopBarHeight(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding() + if (isEditMode) 66.dp else 12.dp
        )
    ) {
        item {
            CityManagerHeader(headerAlpha, headerTitle)
        }
        items(addedCities?.size ?: 0, key = { index ->
            val item = addedCities?.getOrNull(index)
            "${item?.key}-${item?.cityId}"
        }) { index ->
            val item = addedCities?.getOrNull(index)
            val itemKey = "${item?.key}-${item?.cityId}"
            val isLocationCity = item?.isLocationCity ?: false

            val swipeRevealState = rememberSwipeRevealState()
            LaunchedEffect(openedItemKey) {
                if (openedItemKey != itemKey && swipeRevealState.isOpen) {
                    swipeRevealState.close()
                }
            }

            ReorderableItem(
                reorderableLazyListState,
                "${item?.key}-${item?.cityId}",
                enabled = !isLocationCity,
            ) {
                CityManagerItem(
                    item = item,
                    appState = appState,
                    swipeRevealState = swipeRevealState,
                    // 无 item 在拖拽，或者就是当前 item 在拖拽，才允许侧滑
                    enabled = !isEditMode && !isLocationCity && (draggingIndex == null || draggingIndex == index),
                    isEditMode = isEditMode,
                    isSelected = isSelected(item),
                    onItemClick = {
                        if (openedItemKey != null) {
                            openedItemKey = null
                        } else {
                            onItemClick(it)
                        }
                    },
                    onDragStarted = {
                        draggingIndex = index
                        openedItemKey = itemKey
                    },
                    onDragStopped = {
                        if (draggingIndex == index) draggingIndex = null
                    },
                    onSwapDragStarted = {
                        openedItemKey = null
                    },
                    onSwapDragStopped = onSwapDragStopped,
                    toEditMode = toEditMode,
                    removeItem = {
                        openedItemKey = null
                        removeItem(it)
                    },
                    changeDeleteButtonEnable = changeDeleteButtonEnable,
                    itemAlpha = itemAlpha,
                    rippleScale = {
                        if (rippleOriginIndex < 0) 1f
                        else cityRippleScale(abs(index - rippleOriginIndex), rippleElapsedMs())
                    }
                )
            }
        }
    }
}

@Composable
fun CityManagerHeader(
    headerAlpha: Float,
    headerTitle: String
) {
    AppText(
        modifier = Modifier
            .height(60.dp)
            // 这 12dp 的空白正好接住顶栏玻璃的渐隐段，别去掉
            .padding(start = 16.dp, top = 12.dp),
        text = headerTitle,
        fontSize = 28.sp,
        color = colorResource(R.color.black).copy(alpha = headerAlpha),
        fontWeight = FontWeight.Light
    )
}

@Composable
fun ReorderableCollectionItemScope.CityManagerItem(
    item: CityData?,
    appState: AppState,
    swipeRevealState: SwipeRevealState = rememberSwipeRevealState(),
    enabled: Boolean = true,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onItemClick: (cityData: CityData?) -> Unit = {},
    onDragStarted: () -> Unit = {},
    onDragStopped: () -> Unit = {},
    onSwapDragStarted: () -> Unit = {},
    onSwapDragStopped: () -> Unit = {},
    changeDeleteButtonEnable: (enable: Boolean) -> Unit = {},
    toEditMode: (cityData: CityData?) -> Unit,
    removeItem: (cityData: CityData?) -> Unit,
    itemAlpha: Float = 0f,
    rippleScale: () -> Float = { 1f }
) {
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val weatherBg = appState.generateWeatherBg(
        item?.weatherData?.weatherType ?: "",
        Commons.isNight(getToday(), item?.weatherData?.sunrise, item?.weatherData?.sunset),
        true
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
    val isDark = appState.isWeatherHeaderDark(weatherBg)

    val onDragHandleStarted = { _: Offset ->
        changeDeleteButtonEnable(false)
        if (swipeRevealState.isOpen) {
            scope.launch {
                swipeRevealState.close()
            }
        }
        onSwapDragStarted()
        toEditMode(item)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }

    val onDragHandleStopped = {
        changeDeleteButtonEnable(true)
        onSwapDragStopped()
        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
    }

    // 退出编辑模式时把拖拽手柄关掉一帧。
    // 长按 item 进编辑模式后手指不松，另一只手点 x 退出，这个拖拽手势依然活着，
    // 还能接着排序 —— enabled 翻一次才能把它掐断。
    var dragHandleEnabled by remember { mutableStateOf(true) }
    LaunchedEffect(isEditMode) {
        if (isEditMode) return@LaunchedEffect
        dragHandleEnabled = false
        withFrameNanos { }
        dragHandleEnabled = true
    }

    SwipeRevealLayout(
        modifier = Modifier
            // 缩放加在整个侧滑容器上而不是只加在卡片上：卡片单独缩小的话，
            // 背后的删除按钮会从边上露出来
            .graphicsLayer {
                val scale = rippleScale()
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 16.dp),
        revealWidth = 65.dp,
        state = swipeRevealState,
        enabled = enabled,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
        revealContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.color_fe2c3c))
                    .clickable {
                        scope.launch {
                            swipeRevealState.close()
                            removeItem(item)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                CommonIcon(
                    resId = R.mipmap.ic_delete_icon,
                    size = 20.dp,
                    tint = Color.White
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .bounceClick(scalePressed = 0.9f, onClick = {
                        onItemClick(item)
                    }, onLongClick = {
                        if (item?.isLocationCity == true) {
                            // 定位城市不可拖拽排序，没有挂 longPressDraggableHandle，
                            // 也就永远收不到 onDragStopped。
                            // 这里若走 onDragHandleStarted，它禁用掉的删除按钮就再没人恢复，
                            // 之后选中任何城市删除键都是灰的。长按它只该进编辑模式。
                            toEditMode(item)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        } else {
                            onDragHandleStarted(Offset.Zero)
                        }
                    })
                    .then(
                        if (!(item?.isLocationCity ?: false)) {
                            Modifier.longPressDraggableHandle(
                                enabled = dragHandleEnabled,
                                onDragStarted = onDragHandleStarted,
                                onDragStopped = onDragHandleStopped
                            )
                        } else {
                            Modifier
                        }
                    )
                    .fillMaxWidth()
                    .height(Constants.CITY_MANAGER_ITEM_HEIGHT.dp)
                    // 一镜到底里城市卡片整批显隐，不淡入：展开第一帧就藏掉；收回时在卡片落地前
                    // 一下子出来，由涟漪缩放撑出动感（小米天气也是这样）
                    .alpha(itemAlpha)
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(startColor, endColor)),
                        shape = RoundedCornerShape(16.dp)
                    )
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
                CityItem(item = item, isEditMode = isEditMode, isDark = isDark)
                if (!(item?.isLocationCity ?: false)) {
                    EditItem(
                        isEditMode = isEditMode,
                        isSelected = isSelected,
                        isDark = isDark,
                        onDragStarted = onDragHandleStarted,
                        onDragStopped = onDragHandleStopped
                    )
                }
            }
        }
    )
}

@Composable
fun CityItem(
    item: CityData?,
    isEditMode: Boolean = false,
    isDark: Boolean = false
) {
    val isLocationCity = item?.isLocationCity ?: false
    val title = {
        val city = item?.weatherData?.city ?: ""
        val street = item?.street ?: ""
        if (!isLocationCity || street.isEmpty()) city else "$city $street"
    }
    val subTitle = {
        val weatherData = item?.weatherData
        "${weatherData?.weatherDesc} ${Commons.getTemp(weatherData?.tempHigh)} / ${
            Commons.getTemp(weatherData?.tempLow)
        }"
    }
    AppRow(
        modifier = Modifier
            .fillMaxHeight()
            .animateContentSize()
            .padding(horizontal = if (isEditMode && !isLocationCity) 52.dp else 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StartAlignColumn(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp), fillMaxWidth = false
        ) {
            WrapRow {
                AppText(
                    text = title(),
                    fontWeight = FontWeight.Thin,
                    fontSize = 20.sp,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 20.sp,
                        minFontSize = 16.sp,
                        stepSize = 1.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                )
                if (isLocationCity) {
                    CommonIcon(
                        resId = R.mipmap.writing_icon_location1,
                        size = 22.dp,
                        tint = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                    )
                }
            }
            VerticalSpace(4.dp)
            AppText(
                text = subTitle(),
                fontWeight = FontWeight.Thin,
                fontSize = 14.sp,
                color = colorResource(if (isDark) R.color.color_white else R.color.color_black)
            )
        }
        AppText(
            text = Commons.getTemp(item?.weatherData?.temp),
            fontWeight = FontWeight.Thin,
            fontSize = 38.sp,
            maxLines = 1,
            color = colorResource(if (isDark) R.color.color_white else R.color.color_black)
        )
    }
}

@Composable
fun ReorderableCollectionItemScope.EditItem(
    isSelected: Boolean = false,
    isEditMode: Boolean = false,
    isDark: Boolean = false,
    onDragStarted: (startedPosition: Offset) -> Unit = {},
    onDragStopped: () -> Unit = {},
) {
    AnimatedVisibility(visible = isEditMode, enter = fadeIn(), exit = fadeOut()) {
        AppRow(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CommonIcon(
                modifier = Modifier.draggableHandle(
                    onDragStarted = onDragStarted,
                    onDragStopped = onDragStopped
                ),
                resId = R.mipmap.ic_menu_icon,
                size = 24.dp,
                tint = colorResource(if (isDark) R.color.color_white else R.color.color_black)
            )
            CommonIcon(
                modifier = Modifier.background(
                    colorResource(if (isEditMode && isSelected) R.color.color_white else R.color.transparent),
                    shape = CircleShape
                ),
                resId = if (isSelected) R.mipmap.ic_checked_icon else R.mipmap.ic_check_icon,
                size = 22.dp,
                tint = colorResource(if (isSelected) R.color.app_main else if (isDark) R.color.color_white else R.color.color_black)
            )
        }
    }
}

@Composable
fun BottomOperateButton(
    backdrop: Backdrop,
    isEditMode: Boolean = false,
    hasSelected: Boolean = false,
    deleteButtonEnable: Boolean = false,
    removeItems: () -> Unit
) {
    AnimatedVisibility(
        visible = isEditMode,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {},
            // 玻璃比按钮行高出一截用来渐隐，按钮得贴着底边，不能跟着玻璃往上跑
            contentAlignment = Alignment.BottomCenter,
        ) {
            // 和顶栏同一套玻璃，只是把实心的一端翻到下面
            LiquidGlassScrim(
                backdrop = backdrop,
                height = 54.dp + WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 16.dp,
                fromTop = false,
            )
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(54.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                WrapColumn(
                    modifier = if (hasSelected && deleteButtonEnable) Modifier
                        .alphaClick(onClick = removeItems) else Modifier.alpha(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonIcon(
                        resId = R.mipmap.ic_delete_icon,
                        size = 20.dp,
                        tint = colorResource(R.color.black)
                    )
                    AppText(
                        text = "删除",
                        fontSize = 12.sp,
                        color = colorResource(R.color.black)
                    )
                }
            }
        }
    }
}

/** 收回涟漪的时长：离得最远的卡片从最小放大回原尺寸要这么久 */
private const val CITY_RIPPLE_DURATION_MS = 250f

/**
 * 收回时城市卡片的涟漪缩放，distance 是离落地那张隔了几张。
 *
 * 照着小米天气逐帧量出来的：列表出现那一帧卡片就是不透明的，但离落地那张越远越小
 * （隔 2 张 0.92、隔 5 张 0.87），然后所有卡片以同样的速度（每 41ms 放大约 2.5%）长回原尺寸。
 * 近的先到位、远的后到位，看起来就是一道波从落地的卡片往两边扩散。
 * 两家的卡片间距都在 100dp 左右，按张数算的参数可以直接用。
 */
private fun cityRippleScale(distance: Int, elapsedMs: Float): Float {
    if (distance <= 0) return 1f
    val start = (0.957f - 0.0167f * distance).coerceAtLeast(0.85f)
    return (start + 0.000607f * elapsedMs).coerceAtMost(1f)
}
