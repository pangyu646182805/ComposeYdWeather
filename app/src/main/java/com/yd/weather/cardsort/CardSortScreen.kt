package com.yd.weather.cardsort

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.CenterTopAppBar
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.SetStatusBarStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yd.weather.utils.ToastUtils
import com.yd.weather.viewmodel.CardSortViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val ITEM_HEIGHT = 48
private const val ITEM_GAP = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardSortRoute(
    viewModel: CardSortViewModel = hiltViewModel()
) {
    SetStatusBarStyle(isLight = true)
    val weatherCardSort = remember {
        viewModel.getWeatherCardSort()
            .filter { it != Constants.ITEM_TYPE_WEATHER_HEADER }
            .toMutableStateList()
    }
    val observeCardSort = remember {
        viewModel.getObserveCardSort().toMutableStateList()
    }

    // 1f=主列表可见, 0f=专业数据可见
    var contentOpacity by remember { mutableFloatStateOf(1f) }
    // grid 内容独立淡入
    var gridOpacity by remember { mutableFloatStateOf(0f) }
    // "专业数据"标题栏的偏移量（Animatable 支持 snap + animate 两阶段）
    val headerOffset = remember { Animatable(0f) }
    // 记录点击时的偏移量，关闭时回到原位
    var tempHeaderOffsetDp by remember { mutableFloatStateOf(0f) }
    // 拖拽过程中禁用关闭和恢复默认
    var isBackEnabled by remember { mutableStateOf(true) }
    // 是否显示专业数据页（控制组合/移除）
    var isShowObserveGrid by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val animatedContentOpacity by animateFloatAsState(
        targetValue = contentOpacity,
        animationSpec = tween(200),
        label = "contentOpacity"
    )
    val animatedGridOpacity by animateFloatAsState(
        targetValue = gridOpacity,
        animationSpec = tween(200),
        label = "gridOpacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bg_color))
    ) {
        AppColumn(modifier = Modifier.fillMaxSize()) {
            CenterTopAppBar(
                titleText = "卡片排序",
                colors = topAppBarColors(containerColor = colorResource(R.color.bg_color)),
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        enabled = isBackEnabled
                    ) {
                        CommonIcon(
                            resId = R.mipmap.ic_close_icon1,
                            size = 20.dp,
                            tint = colorResource(R.color.black).copy(
                                alpha = if (isBackEnabled) 1f else 0.2f
                            )
                        )
                    }
                },
                actions = {
                    AppText(
                        modifier = Modifier
                            .then(
                                if (isBackEnabled) Modifier.alphaClick {
                                    resetToDefault(
                                        weatherCardSort,
                                        observeCardSort,
                                        viewModel
                                    )
                                } else Modifier
                            )
                            .padding(end = 16.dp),
                        text = "恢复默认",
                        fontSize = 15.sp,
                        color = colorResource(R.color.black).copy(
                            alpha = if (isBackEnabled) 1f else 0.2f
                        )
                    )
                },
                showBackIcon = false
            )

            Box(modifier = Modifier.weight(1f)) {
                // 主卡片排序列表
                WeatherCardSortList(
                    modifier = Modifier.alpha(animatedContentOpacity),
                    weatherCardSort = weatherCardSort,
                    onMove = { from, to ->
                        weatherCardSort.apply { add(to, removeAt(from)) }
                    },
                    onDragStarted = { isBackEnabled = false },
                    onDragStopped = {
                        isBackEnabled = true
                        val sort =
                            listOf(Constants.ITEM_TYPE_WEATHER_HEADER) + weatherCardSort
                        viewModel.saveWeatherCardSort(sort.toIntArray())
                    },
                    onObserveSortTap = { index ->
                        scope.launch {
                            val offset = (index * (ITEM_HEIGHT + ITEM_GAP)).toFloat()
                            tempHeaderOffsetDp = offset
                            // 阶段1：header snap 到卡片位置（必须在显示 grid 前完成）
                            headerOffset.snapTo(offset)
                            // 阶段2：显示 grid 层，隐藏主列表（crossfade）
                            isShowObserveGrid = true
                            contentOpacity = 0f
                            // 阶段3：header 滑到顶部 + grid 内容淡入（并行）
                            gridOpacity = 1f
                            headerOffset.animateTo(0f, tween(200))
                        }
                    }
                )

                // 专业数据排序 Grid（参照 Flutter Offstage，始终保留在组合树中）
                if (isShowObserveGrid) {
                    ObserveCardSortGrid(
                        modifier = Modifier
                            .alpha(1f - animatedContentOpacity)
                            .background(colorResource(R.color.bg_color)),
                        gridOpacity = animatedGridOpacity,
                        headerOffset = headerOffset.value.dp,
                        observeCardSort = observeCardSort,
                        onMove = { from, to ->
                            observeCardSort.apply { add(to, removeAt(from)) }
                        },
                        onDragStarted = { isBackEnabled = false },
                        onDragStopped = {
                            isBackEnabled = true
                            viewModel.saveObserveCardSort(observeCardSort.toIntArray())
                        },
                        onClose = {
                            scope.launch {
                                // 阶段1：grid 内容淡出 + header 滑回原位（并行 200ms）
                                gridOpacity = 0f
                                headerOffset.animateTo(tempHeaderOffsetDp, tween(200))
                                // 阶段2：主列表淡入 + observe 容器淡出（crossfade 200ms）
                                contentOpacity = 1f
                                delay(200)
                                // 阶段3：隐藏
                                isShowObserveGrid = false
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun resetToDefault(
    weatherCardSort: MutableList<Int>,
    observeCardSort: MutableList<Int>,
    viewModel: CardSortViewModel
) {
    val current = listOf(Constants.ITEM_TYPE_WEATHER_HEADER) + weatherCardSort
    val isCardSortChanged = !current.toIntArray()
        .contentEquals(Constants.DEFAULT_WEATHER_CARD_SORT)
    val isObserveSortChanged = !observeCardSort.toIntArray()
        .contentEquals(Constants.DEFAULT_WEATHER_OBSERVES_CARD_SORT)
    if (isCardSortChanged || isObserveSortChanged) {
        val defaultCardSort = Constants.DEFAULT_WEATHER_CARD_SORT.toList()
        val defaultObserveSort = Constants.DEFAULT_WEATHER_OBSERVES_CARD_SORT.toList()
        weatherCardSort.clear()
        weatherCardSort.addAll(defaultCardSort.filter { it != Constants.ITEM_TYPE_WEATHER_HEADER })
        observeCardSort.clear()
        observeCardSort.addAll(defaultObserveSort)
        viewModel.saveWeatherCardSort(defaultCardSort.toIntArray())
        viewModel.saveObserveCardSort(defaultObserveSort.toIntArray())
    }
    ToastUtils.show("已恢复默认")
}

@Composable
private fun WeatherCardSortList(
    modifier: Modifier = Modifier,
    weatherCardSort: List<Int>,
    onMove: (from: Int, to: Int) -> Unit,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    onObserveSortTap: (index: Int) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    AppColumn(modifier = modifier.fillMaxSize()) {
        SortDescription("首页的天气卡片将会按照以下排序进行展示")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(ITEM_GAP.dp)
        ) {
            itemsIndexed(weatherCardSort, key = { _, item -> item }) { index, itemType ->
            ReorderableItem(reorderableState, itemType) { isDragging ->
                val elevation by animateDpAsState(
                    if (isDragging) 4.dp else 0.dp, label = "elevation"
                )
                SortCardItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .shadow(elevation, RoundedCornerShape(6.dp))
                        .then(
                            if (itemType == Constants.ITEM_TYPE_OBSERVE)
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onObserveSortTap(index)
                                }
                            else Modifier
                        )
                        .longPressDraggableHandle(
                            onDragStarted = { onDragStarted() },
                            onDragStopped = onDragStopped
                        ),
                    title = getWeatherCardTitle(itemType),
                    showSortIcon = itemType == Constants.ITEM_TYPE_OBSERVE,
                    dragHandle = {
                        CommonIcon(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = { onDragStarted() },
                                onDragStopped = onDragStopped
                            ),
                            resId = R.mipmap.ic_menu_icon,
                            size = 24.dp,
                            tint = colorResource(R.color.color_999999)
                        )
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun ObserveCardSortGrid(
    modifier: Modifier = Modifier,
    gridOpacity: Float,
    headerOffset: androidx.compose.ui.unit.Dp = 0.dp,
    observeCardSort: List<Int>,
    onMove: (from: Int, to: Int) -> Unit,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    onClose: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        onMove(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    AppColumn(modifier = modifier.fillMaxSize()) {
        SortDescription("专业数据卡片将会按照以下排序进行展示")

        // margin 占位（参照 Flutter AnimatedContainer margin.top）
        if (headerOffset > 0.dp) {
            Spacer(modifier = Modifier.height(headerOffset))
        }

        // "专业数据"标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(ITEM_HEIGHT.dp)
                .background(
                    colorResource(R.color.card_color_06),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = "专业数据",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.black)
                )
                HorizontalSpace(width = 8.dp)
                CommonIcon(
                    resId = R.mipmap.ic_sort_icon,
                    size = 18.dp,
                    tint = colorResource(R.color.color_999999)
                )
            }
            CommonIcon(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClose
                ),
                resId = R.mipmap.ic_close_icon1,
                size = 18.dp,
                tint = colorResource(R.color.color_999999)
            )
        }

        VerticalSpace(height = ITEM_GAP.dp)

        // Grid 内容（独立淡入）
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .alpha(gridOpacity),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(ITEM_GAP.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_GAP.dp)
        ) {
            itemsIndexed(observeCardSort, key = { _, item -> item }) { _, itemType ->
                ReorderableItem(reorderableState, itemType) { isDragging ->
                    val elevation by animateDpAsState(
                        if (isDragging) 4.dp else 0.dp, label = "elevation"
                    )
                    SortCardItem(
                        modifier = Modifier
                            .shadow(elevation, RoundedCornerShape(6.dp))
                            .longPressDraggableHandle(
                                onDragStarted = { onDragStarted() },
                                onDragStopped = onDragStopped
                            ),
                        title = getObserveCardTitle(itemType),
                        dragHandle = {
                            CommonIcon(
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = { onDragStarted() },
                                    onDragStopped = onDragStopped
                                ),
                                resId = R.mipmap.ic_menu_icon,
                                size = 24.dp,
                                tint = colorResource(R.color.color_999999)
                            )
                        }
                    )
                }
            }
        }
    }
}

/** 排序说明文字 */
@Composable
private fun SortDescription(text: String) {
    AppText(
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        text = text,
        fontSize = 14.sp,
        color = colorResource(R.color.color_999999)
    )
}

/** 可复用的排序卡片项 */
@Composable
private fun SortCardItem(
    modifier: Modifier = Modifier,
    title: String,
    showSortIcon: Boolean = false,
    dragHandle: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT.dp)
            .background(
                colorResource(R.color.card_color_06),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.black)
            )
            if (showSortIcon) {
                HorizontalSpace(width = 8.dp)
                CommonIcon(
                    resId = R.mipmap.ic_sort_icon,
                    size = 18.dp,
                    tint = colorResource(R.color.color_999999)
                )
            }
        }
        dragHandle()
    }
}

private fun getWeatherCardTitle(itemType: Int): String = when (itemType) {
    Constants.ITEM_TYPE_ALARMS -> "极端天气"
    Constants.ITEM_TYPE_AIR_QUALITY -> "空气质量"
    Constants.ITEM_TYPE_HOUR_WEATHER -> "每小时天气预报"
    Constants.ITEM_TYPE_DAILY_WEATHER -> "15日天气预报"
    Constants.ITEM_TYPE_OBSERVE -> "专业数据"
    Constants.ITEM_TYPE_LIFE_INDEX -> "生活指数"
    else -> ""
}

private fun getObserveCardTitle(itemType: Int): String = when (itemType) {
    Constants.ITEM_TYPE_OBSERVE_UV -> "紫外线指数"
    Constants.ITEM_TYPE_OBSERVE_SHI_DU -> "湿度"
    Constants.ITEM_TYPE_OBSERVE_TI_GAN -> "体感温度"
    Constants.ITEM_TYPE_OBSERVE_WD -> "风向"
    Constants.ITEM_TYPE_OBSERVE_SUNRISE_SUNSET -> "日出日落"
    Constants.ITEM_TYPE_OBSERVE_PRESSURE -> "气压"
    Constants.ITEM_TYPE_OBSERVE_VISIBILITY -> "可见度"
    Constants.ITEM_TYPE_OBSERVE_FORECAST40 -> "未来40日天气"
    else -> ""
}
