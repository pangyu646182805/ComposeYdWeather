package com.yd.weather.weatherbgedit

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.R
import com.yd.weather.app.AppState
import com.yd.weather.component.AppText
import com.yd.weather.component.alphaClick
import com.yd.weather.component.bounceClick
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.SetStatusBarStyle
import com.yd.weather.widget.WeatherCitySnapshot

@Composable
internal fun WeatherBgEditRoute(
    route: com.yd.weather.routes.WeatherBgRoutes.WeatherBgEdit? = null,
    viewModel: WeatherBgEditViewModel = hiltViewModel()
) {
    // 传入路由参数并触发初始化（同步调用，确保首帧前数据就绑好）
    if (route != null) viewModel.initialize(route)

    val isNight by viewModel.isNight.collectAsStateWithLifecycle()
    val colors by viewModel.colors.collectAsStateWithLifecycle()
    val nightColors by viewModel.nightColors.collectAsStateWithLifecycle()
    val hsvColors by viewModel.hsvColors.collectAsStateWithLifecycle()
    val hsvNightColors by viewModel.hsvNightColors.collectAsStateWithLifecycle()
    val isStartSelected by viewModel.isStartSelected.collectAsStateWithLifecycle()
    val isPreviewMode = viewModel.isPreviewMode

    val appState = viewModel.appState()
    val currentCityData by appState.currentCityData.collectAsStateWithLifecycle()
    val snapshotData = remember(currentCityData) {
        generateSnapshotData(appState, currentCityData)
    }

    var showColorInputDialog by remember { mutableStateOf(false) }

    WeatherBgEditScreen(
        isNight = isNight,
        isPreviewMode = isPreviewMode,
        isStartSelected = isStartSelected,
        colors = colors,
        nightColors = nightColors,
        hsvColors = hsvColors,
        hsvNightColors = hsvNightColors,
        snapshotData = snapshotData,
        onToggleNight = { viewModel.toggleNight(it) },
        onSelectStart = { viewModel.selectStart(it) },
        onLabelClick = { isStart ->
            if ((isStart && isStartSelected) || (!isStart && !isStartSelected)) {
                showColorInputDialog = true
            } else {
                viewModel.selectStart(isStart)
            }
        },
        onUpdateHue = { viewModel.updateHue(it) },
        onUpdateSaturation = { viewModel.updateSaturation(it) },
        onUpdateValue = { viewModel.updateValue(it) },
        onCancel = { viewModel.navigateBack() },
        onConfirm = { viewModel.confirm() }
    )

    // 颜色输入对话框（Dialog 拦截返回键）
    if (showColorInputDialog) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            // 去掉 Dialog 自带的遮罩
            (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0.4f)
            ColorInputDialog(
                onDismiss = { showColorInputDialog = false },
                onColorInput = { hex ->
                    viewModel.updateColorFromHex(hex)
                    showColorInputDialog = false
                }
            )
        }
    }
}

private fun generateSnapshotData(appState: AppState, cityData: CityData?): SnapshotData {
    val key =
        if (cityData?.isLocationCity == true) Constants.LOCATION_CITY_ID else cityData?.cityId ?: ""
    val weatherData = appState.getWeatherData(key)
    val itemTypeObserves = appState.getItemTypeObserves(
        appState.currentWeatherObservesCardSort.value,
        Constants.ITEM_TYPE_OBSERVE, weatherData
    )
    return SnapshotData(
        appState = appState,
        cityData = cityData,
        itemTypeObserves = itemTypeObserves,
        weatherItems = appState.generateWeatherItems(itemTypeObserves, weatherData)
    )
}

private class SnapshotData(
    val appState: AppState,
    val cityData: CityData?,
    val itemTypeObserves: Array<Int>?,
    val weatherItems: List<WeatherItemData>?
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun WeatherBgEditScreen(
    isNight: Boolean,
    isPreviewMode: Boolean,
    isStartSelected: Boolean,
    colors: List<Color>,
    nightColors: List<Color>,
    hsvColors: List<FloatArray>,
    hsvNightColors: List<FloatArray>,
    snapshotData: SnapshotData,
    onToggleNight: (Boolean) -> Unit,
    onSelectStart: (Boolean) -> Unit,
    onLabelClick: (Boolean) -> Unit,
    onUpdateHue: (Float) -> Unit,
    onUpdateSaturation: (Float) -> Unit,
    onUpdateValue: (Float) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    SetStatusBarStyle(isLight = true)
    val displayColors = if (isNight) nightColors else colors
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bg_color))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 日间/夜间切换 Tab（参照 Flutter: 64.w 高度，32.w 水平内边距，12.w 垂直 margin）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 32.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dayBg by animateColorAsState(
                targetValue = if (!isNight) colorResource(R.color.black).copy(alpha = 0.25f) else Color.Transparent,
                animationSpec = tween(400), label = "dayBg"
            )
            val nightBg by animateColorAsState(
                targetValue = if (isNight) colorResource(R.color.black).copy(alpha = 0.25f) else Color.Transparent,
                animationSpec = tween(400), label = "nightBg"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .alphaClick(onClick = { if (isNight) onToggleNight(false) })
                    .clip(RoundedCornerShape(16.dp))
                    .background(dayBg),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "日间",
                    fontSize = 18.sp,
                    color = colorResource(R.color.black),
                    fontWeight = if (!isNight) FontWeight.Bold else null
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .alphaClick(onClick = { if (!isNight) onToggleNight(true) })
                    .clip(RoundedCornerShape(16.dp))
                    .background(nightBg),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "夜间",
                    fontSize = 18.sp,
                    color = colorResource(R.color.black),
                    fontWeight = if (isNight) FontWeight.Bold else null
                )
            }
        }

        // 天气预览快照（参照 Flutter: Expanded + LayoutBuilder 计算 scale）
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            // 参照 Flutter: scale = (constraints.maxHeight - 16) / screenHeight
            val availableHeightPx = with(density) { (maxHeight - 16.dp).toPx() }
            val screenHeightPx = with(density) { screenHeightDp.dp.toPx() }
            val scale = (availableHeightPx / screenHeightPx).coerceIn(0.1f, 1f)
            val scaledWidthDp = with(density) { (screenWidthDp.dp.toPx() * scale).toDp() }

            val previewColors = if (displayColors.size >= 2) displayColors
            else listOf(Color.Gray, Color.DarkGray)
            val isDark = snapshotData.appState.isDark(previewColors)
            val isWeatherHeaderDark = snapshotData.appState.isWeatherHeaderDark(previewColors)
            val panelOpacity = snapshotData.appState.calPanelOpacity(previewColors)

            WeatherCitySnapshot(
                cityData = snapshotData.cityData,
                weatherItems = snapshotData.weatherItems,
                weatherBg = previewColors,
                isDark = isDark,
                isWeatherHeaderDark = isWeatherHeaderDark,
                panelOpacity = panelOpacity,
                itemTypeObserves = snapshotData.itemTypeObserves,
                scale = scale
            )

            // 颜色 HEX 标签覆盖在快照上（参照 Flutter: 宽度=屏幕宽*scale, top=138, bottom=132）
            if (!isPreviewMode && displayColors.size >= 2) {
                val scaledHeightDp = with(density) { (screenHeightPx * scale).toDp() }
                val labelTopPadding = with(density) { (138.dp.toPx() * scale).toDp() }
                val labelBottomPadding = with(density) { (132.dp.toPx() * scale).toDp() }
                Column(
                    modifier = Modifier
                        .width(scaledWidthDp)
                        .height(scaledHeightDp)
                        .padding(
                            top = labelTopPadding,
                            bottom = labelBottomPadding
                        ),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ColorLabel(
                        color = displayColors[0],
                        isSelected = isStartSelected,
                        arrowDirection = ArrowDirection.Bottom,
                        onClick = { onLabelClick(true) }
                    )
                    ColorLabel(
                        color = displayColors[1],
                        isSelected = !isStartSelected,
                        arrowDirection = ArrowDirection.Top,
                        onClick = { onLabelClick(false) }
                    )
                }
            }
        }

        // HSV 色相/饱和度/亮度滑块（参照 Flutter: 独立更新 H/S/V，互不影响）
        if (!isPreviewMode) {
            val index = if (isStartSelected) 0 else 1
            val currentHsvList = if (isNight) hsvNightColors else hsvColors
            val hsv = currentHsvList.getOrNull(index)
            if (hsv != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 色相
                    AppText(
                        text = "色相：", fontSize = 14.sp,
                        color = colorResource(R.color.black), fontWeight = FontWeight.Bold
                    )
                    HsvSlider(
                        value = hsv[0] / 360f,
                        colors = hueGradientColors(),
                        onValueChange = { onUpdateHue(it * 360f) }
                    )

                    Spacer(Modifier.height(12.dp))

                    // 饱和度（渐变色跟随当前色相）
                    AppText(
                        text = "饱和度：", fontSize = 14.sp,
                        color = colorResource(R.color.black), fontWeight = FontWeight.Bold
                    )
                    HsvSlider(
                        value = hsv[1],
                        colors = listOf(
                            Color.White,
                            WeatherBgEditViewModel.hsvToColor(floatArrayOf(hsv[0], 1f, 1f))
                        ),
                        onValueChange = { onUpdateSaturation(it) }
                    )

                    Spacer(Modifier.height(12.dp))

                    // 亮度
                    AppText(
                        text = "亮度：", fontSize = 14.sp,
                        color = colorResource(R.color.black), fontWeight = FontWeight.Bold
                    )
                    HsvSlider(
                        value = hsv[2],
                        colors = listOf(Color.Black, Color.White),
                        onValueChange = { onUpdateValue(it) }
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            // 取消/确定按钮（参照 Flutter: 64.w 高度，两个 Expanded 均分）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .alphaClick(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "取消", fontSize = 18.sp,
                        color = colorResource(R.color.black), fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .alphaClick(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = "确定", fontSize = 18.sp,
                        color = colorResource(R.color.black), fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private enum class ArrowDirection { Top, Bottom }

/**
 * 颜色 HEX 标签（参照 Flutter BubbleBox）
 * 带三角箭头的气泡框，箭头居中
 */
@Composable
private fun ColorLabel(
    color: Color,
    isSelected: Boolean,
    arrowDirection: ArrowDirection,
    onClick: () -> Unit
) {
    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    val bgColor = if (isSelected) Color(0xFFD5D5D5) else Color.White
    val arrowSize = 6.dp
    val arrowSizePx = with(LocalDensity.current) { arrowSize.toPx() }

    Column(
        modifier = Modifier.bounceClick(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部箭头（arrowDirection == Top 时显示）
        if (arrowDirection == ArrowDirection.Top) {
            Canvas(modifier = Modifier.size(width = arrowSize * 2, height = arrowSize)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, bgColor)
            }
        }

        // 文字内容
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            AppText(text = hex, fontSize = 15.sp, color = Color(0xFF333333))
        }

        // 底部箭头（arrowDirection == Bottom 时显示）
        if (arrowDirection == ArrowDirection.Bottom) {
            Canvas(modifier = Modifier.size(width = arrowSize * 2, height = arrowSize)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2, size.height)
                    close()
                }
                drawPath(path, bgColor)
            }
        }
    }
}

/**
 * 自定义 HSV 滑块（参照 Flutter WeatherBgColorSelector）
 * 渐变圆角条 + 白色圆形滑块（带灰色边框），手势拖动控制
 */
@Composable
private fun HsvSlider(
    value: Float,
    colors: List<Color>,
    onValueChange: (Float) -> Unit
) {
    val sliderHeight = 20.dp
    val thumbSize = sliderHeight
    val clampedValue = value.coerceIn(0f, 1f)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(sliderHeight)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val percent = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(percent)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val percent = (change.position.x / size.width).coerceIn(0f, 1f)
                        onValueChange(percent)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val percent = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(percent)
                }
            }
    ) {
        // 渐变条背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(colors))
        )

        // 圆形滑块（白色 + 灰色边框）
        val thumbSizePx = with(density) { thumbSize.toPx() }
        val maxOffsetPx = constraints.maxWidth - thumbSizePx
        val offsetX = with(density) { (maxOffsetPx * clampedValue).toDp() }

        Box(
            modifier = Modifier
                .padding(start = offsetX)
                .size(thumbSize)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFCCCCCC), CircleShape)
                .background(Color.White)
        )
    }
}

private fun hueGradientColors(): List<Color> {
    return listOf(
        Color(0xFFFF0000.toInt()), Color(0xFFFFFF00.toInt()), Color(0xFF00FF00.toInt()),
        Color(0xFF00FFFF.toInt()), Color(0xFF0000FF.toInt()), Color(0xFFFF00FF.toInt()),
        Color(0xFFFF0000.toInt())
    )
}

/**
 * 颜色 HEX 输入对话框（参照 Flutter ColorInputDialog）
 * 两段式动画：内容面板 slideIn → 200ms 后键盘 slideIn
 * 退场反向：键盘 slideOut → 200ms 后内容面板 slideOut → 关闭
 */
@Composable
private fun ColorInputDialog(
    onDismiss: () -> Unit,
    onColorInput: (String) -> Unit
) {
    val inputKeys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "A", "B", "C",
        "D", "E", "F",
        "", "0", "del"
    )
    var currentInput by remember { mutableStateOf(List(6) { "" }) }
    var currentIndex by remember { mutableIntStateOf(0) }

    // 两段式动画状态
    var contentVisible by remember { mutableStateOf(false) }
    var keyboardVisible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }

    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // 退场动画：键盘滑出 → 200ms → 内容滑出 → 200ms → 关闭
    fun animateDismiss(onFinish: () -> Unit) {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            keyboardVisible = false
            delay(200)
            contentVisible = false
            delay(200)
            onFinish()
        }
    }

    // 返回键走动画退场
    BackHandler { animateDismiss(onDismiss) }

    // 进场动画：内容滑入 → 200ms → 键盘滑入
    LaunchedEffect(Unit) {
        contentVisible = true
        delay(200)
        keyboardVisible = true
    }

    // 全屏半透明遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { animateDismiss(onDismiss) } },
        contentAlignment = Alignment.BottomCenter
    ) {
        // 内容面板（从底部滑入/滑出）
        AnimatedVisibility(
            visible = contentVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(200)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(colorResource(R.color.bg_color))
                    .pointerInput(Unit) { detectTapGestures { /* 拦截点击不穿透 */ } },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // HEX 输入结果显示 #XXXXXX
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InputResultCell("#")
                    currentInput.forEach { char ->
                        InputResultCell(char)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 键盘区域（固定高度 + 内部 slideIn）
                Box(modifier = Modifier.height(54.dp * 6 + 0.5.dp)) {
                    this@Column.AnimatedVisibility(
                        visible = keyboardVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(200)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(200)
                        )
                    ) {
                        Column {
                            // 分隔线
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(
                                        colorResource(R.color.black).copy(alpha = 0.06f)
                                    )
                            )

                            // 键盘 6行3列
                            for (row in 0 until 6) {
                                Row(Modifier.fillMaxWidth()) {
                                    for (col in 0 until 3) {
                                        val key = inputKeys[row * 3 + col]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(54.dp)
                                                .border(
                                                    0.5.dp,
                                                    colorResource(R.color.black)
                                                        .copy(alpha = 0.06f)
                                                )
                                                .then(
                                                    if (key.isNotEmpty()) Modifier.alphaClick(
                                                        onClick = {
                                                            view.performHapticFeedback(
                                                                HapticFeedbackConstants.CLOCK_TICK
                                                            )
                                                            if (key == "del") {
                                                                if (currentIndex > 0) {
                                                                    currentIndex--
                                                                    currentInput =
                                                                        currentInput.toMutableList()
                                                                            .also {
                                                                                it[currentIndex] = ""
                                                                            }
                                                                }
                                                            } else if (currentIndex <= 5) {
                                                                currentInput =
                                                                    currentInput.toMutableList()
                                                                        .also {
                                                                            it[currentIndex] = key
                                                                        }
                                                                currentIndex++
                                                                // 6 位输满：键盘滑出再回调
                                                                if (currentIndex == 6) {
                                                                    val hex =
                                                                        currentInput.joinToString("")
                                                                    animateDismiss {
                                                                        onColorInput(hex)
                                                                    }
                                                                }
                                                            }
                                                        }) else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (key.isNotEmpty()) {
                                                AppText(
                                                    text = if (key == "del") "删除" else key,
                                                    fontSize = if (key == "del") 20.sp else 26.sp,
                                                    color = colorResource(R.color.black),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 导航栏区域（背景色延伸到导航栏）
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun InputResultCell(text: String) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(R.color.black).copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = text.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppText(
                text = text,
                fontSize = 22.sp,
                color = colorResource(R.color.black),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

