package com.yd.weather.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drake.logcat.LogCat
import com.yd.weather.R
import com.yd.weather.app.AppState
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppScaffold
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
import com.yd.weather.viewmodel.CityManagerViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CityManagerPage(
    addedCities: List<CityData>? = null,
    swap: (fromIndex: Int, toIndex: Int) -> Unit,
    onSwapDragStopped: () -> Unit = {},
    removeCityData: (cityData: CityData?, block: () -> Unit) -> Unit,
    removeCities: (cities: List<CityData>?, block: () -> Unit) -> Unit,
    viewModel: CityManagerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val selectedList by viewModel.selectedList.collectAsStateWithLifecycle()
    val deleteButtonEnable by viewModel.deleteButtonEnable.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
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

    AppScaffold(
        titleText = title,
        titleAlpha = centerTitleAlpha,
        navigationIcon = {
            LeftIcon(isEditMode = isEditMode) {
                if (isEditMode) {
                    viewModel.closeEditMode()
                }
            }
        },
        topBarActions = {
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
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            CityList(
                addedCities = addedCities,
                appState = viewModel.appState(),
                isEditMode = isEditMode,
                isSelected = { cityData ->
                    viewModel.isSelected(cityData)
                },
                scrollState = scrollState,
                headerAlpha = headerAlpha,
                headerTitle = title,
                swap = swap,
                onSwapDragStopped = onSwapDragStopped,
                toEditMode = { cityData ->
                    viewModel.toEditMode(addedCities?.size ?: 0, cityData)
                },
                removeItem = { cityData ->
                    cityData ?: return@CityList
                    removeCityData(cityData) {
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
                    }
                },
                changeDeleteButtonEnable = { enable ->
                    viewModel.changeDeleteButtonEnable(enable)
                }
            )
            BottomOperateButton(
                isEditMode = isEditMode,
                hasSelected = viewModel.hasSelected(),
                deleteButtonEnable = deleteButtonEnable,
                removeItems = {
                    removeCities(selectedList.toList()) {
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
) {
    // 当前正在拖拽的 item index，null 表示无 item 在拖拽
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    var openedItemKey by remember { mutableStateOf<String?>(null) }

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
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
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
                    onItemClick = onItemClick,
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
                    changeDeleteButtonEnable = changeDeleteButtonEnable
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
    removeItem: (cityData: CityData?) -> Unit
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
    val isDark = appState.isDark(weatherBg)

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

    SwipeRevealLayout(
        modifier = Modifier.padding(horizontal = 16.dp),
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
                    .bounceClick(scalePressed = 0.9f) {
                        onItemClick(item)
                    }
                    .then(
                        if (item?.isLocationCity ?: false) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = onDragHandleStarted
                                )
                            }
                        } else {
                            Modifier.longPressDraggableHandle(
                                onDragStarted = onDragHandleStarted,
                                onDragStopped = onDragHandleStopped
                            )
                        }
                    )
                    .fillMaxWidth()
                    .height(Constants.CITY_MANAGER_ITEM_HEIGHT.dp)
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(startColor, endColor)),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
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
    isDark: Boolean = false,
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
        StartAlignColumn(fillMaxWidth = false) {
            WrapRow {
                AppText(
                    text = title(),
                    fontWeight = FontWeight.Thin,
                    fontSize = 20.sp,
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
    isEditMode: Boolean = false,
    hasSelected: Boolean = false,
    deleteButtonEnable: Boolean = false,
    removeItems: () -> Unit
) {
    AnimatedVisibility(
        visible = isEditMode,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.bg_color))
                .navigationBarsPadding()
                .pointerInput(Unit) {}
        ) {
            Box(
                modifier = Modifier
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
