package com.yd.weather.weatherbglist

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.component.CenterTopAppBar
import com.yd.weather.component.bounceClick
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.SetStatusBarStyle
import com.yd.weather.utils.rememberElasticScrollState
import com.yd.weather.utils.toSafeColor
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun WeatherBgListRoute(
    viewModel: WeatherBgListViewModel = hiltViewModel()
) {
    // 从编辑页返回时重新加载数据
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadWeatherBgMap()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val weatherBgMap by viewModel.weatherBgMap.collectAsStateWithLifecycle()
    val isNight by viewModel.isNight.collectAsStateWithLifecycle()
    val isShowMenu by viewModel.isShowMenu.collectAsStateWithLifecycle()

    WeatherBgListScreen(
        weatherBgMap = weatherBgMap,
        isNight = isNight,
        isShowMenu = isShowMenu,
        onBack = { viewModel.navigateBack() },
        onToggleNight = { viewModel.toggleNight() },
        onRemoveAll = { viewModel.removeAllWeatherBg() },
        onToggleMenu = { viewModel.toggleMenu() },
        onHideMenu = { viewModel.hideMenu() },
        onSelectBg = { type, model -> viewModel.setCurrentWeatherBg(type, model) },
        onRemoveBg = { type, model -> viewModel.removeWeatherBg(type, model) },
        onEditBg = { type, model -> viewModel.navigateToWeatherBgEdit(type, model, isEdit = true) },
        onPreviewBg = { type, model ->
            viewModel.navigateToWeatherBgEdit(
                type,
                model,
                isPreviewMode = true
            )
        },
        onAddBg = { type, selectedModel -> viewModel.navigateToWeatherBgEdit(type, selectedModel) }
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WeatherBgListScreen(
    weatherBgMap: Map<String, List<WeatherBgModel>>,
    isNight: Boolean,
    isShowMenu: Boolean,
    onBack: () -> Unit,
    onToggleNight: () -> Unit,
    onRemoveAll: () -> Unit,
    onToggleMenu: () -> Unit,
    onHideMenu: () -> Unit,
    onSelectBg: (String, WeatherBgModel) -> Unit,
    onRemoveBg: (String, WeatherBgModel) -> Unit,
    onEditBg: (String, WeatherBgModel) -> Unit,
    onPreviewBg: (String, WeatherBgModel) -> Unit,
    onAddBg: (String, WeatherBgModel?) -> Unit
) {
    SetStatusBarStyle(isLight = true)
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val itemAspectRatio = screenWidth.toFloat() / screenHeight.toFloat()
    val view = LocalView.current

    val elastic = rememberElasticScrollState(orientation = Orientation.Vertical)
    val lazyListState = rememberLazyListState()

    // 滚动时隐藏菜单（参照 Flutter NotificationListener）
    LaunchedEffect(isShowMenu) {
        if (isShowMenu) {
            snapshotFlow { lazyListState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    if (scrolling) onHideMenu()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bg_color))
    ) {
        CenterTopAppBar(
            titleText = "天气背景",
            colors = topAppBarColors(containerColor = colorResource(R.color.transparent)),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    CommonIcon(
                        resId = R.mipmap.ic_close_icon1,
                        size = 20.dp,
                        tint = colorResource(R.color.black),
                    )
                }
            },
            showBackIcon = false,
            actions = {
                TextButton(onClick = {
                    if (isShowMenu) onRemoveAll() else onToggleNight()
                }) {
                    AppText(
                        text = if (isShowMenu) "全部删除" else if (isNight) "日间" else "夜间",
                        color = colorResource(R.color.black),
                        fontSize = 14.sp
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
        ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(elastic.connection)
                .graphicsLayer { translationY = elastic.overscrollOffset }
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(weatherBgMap.keys.toList(), key = { it }) { weatherType ->
                val models = weatherBgMap[weatherType] ?: emptyList()
                val selectedItem = models.firstOrNull { it.isSelected }
                val showAddItem = models.size < Constants.MAX_WEATHER_BG_COUNT

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    AppText(
                        text = weatherTypeTitle(weatherType),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.black)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 3
                    ) {
                        models.forEach { model ->
                            WeatherBgGridItem(
                                modifier = Modifier.weight(1f),
                                model = model,
                                isNight = isNight,
                                isShowMenu = isShowMenu,
                                itemAspectRatio = itemAspectRatio,
                                onLongPress = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    onToggleMenu()
                                },
                                onClick = {
                                    if (isShowMenu) {
                                        onHideMenu()
                                    } else if (!model.isSelected) {
                                        onSelectBg(weatherType, model)
                                    }
                                },
                                onEdit = { onEditBg(weatherType, model) },
                                onPreview = { onPreviewBg(weatherType, model) },
                                onRemove = { onRemoveBg(weatherType, model) }
                            )
                        }
                        // 添加按钮
                        if (showAddItem) {
                            WeatherBgAddItem(
                                modifier = Modifier.weight(1f),
                                itemAspectRatio = itemAspectRatio,
                                onClick = {
                                    if (isShowMenu) {
                                        onHideMenu()
                                    } else {
                                        onAddBg(weatherType, selectedItem)
                                    }
                                }
                            )
                        }
                        // 占位：不足 3 列时补空位保持对齐
                        val totalCount = models.size + if (showAddItem) 1 else 0
                        val remainder = totalCount % 3
                        if (remainder != 0) {
                            repeat(3 - remainder) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun WeatherBgGridItem(
    modifier: Modifier = Modifier,
    model: WeatherBgModel,
    isNight: Boolean,
    isShowMenu: Boolean,
    itemAspectRatio: Float,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = if (isNight) model.nightColors else model.colors
    // 参照 Flutter: 用原始颜色判断相似度（非动画中间值）
    val color1 = colors.getOrElse(0) { 0xFFFFFFFFu }.toSafeColor()
    val color2 = colors.getOrElse(1) { 0xFFFFFFFFu }.toSafeColor()
    val bgColor = colorResource(R.color.bg_color)
    val similarity1 = calSimilarity(bgColor, color1)
    val similarity2 = calSimilarity(bgColor, color2)
    val needBorder = similarity1 > 0.95f && similarity2 > 0.95f
    // 参照 Flutter: ThemeData.estimateBrightnessForColor(color1) == Brightness.dark
    val lum = color1.luminance()
    val iconDark = (lum + 0.05f).let { it * it } <= 0.15f
    // 日间/夜间切换渐变色动画（参照 Flutter AnimatedContainer duration: 400ms）
    val animatedColor1 by animateColorAsState(
        targetValue = color1,
        animationSpec = tween(400),
        label = "gradientColor1"
    )
    val animatedColor2 by animateColorAsState(
        targetValue = color2,
        animationSpec = tween(400),
        label = "gradientColor2"
    )
    val gradientColors = listOf(animatedColor1, animatedColor2)

    Box(
        modifier = modifier
            .aspectRatio(itemAspectRatio)
            .bounceClick(
                scalePressed = 0.95f,
                onClick = onClick,
                onLongClick = onLongPress
            )
            .then(
                if (needBorder) Modifier.border(
                    1.dp,
                    colorResource(R.color.black),
                    RoundedCornerShape(16.dp)
                )
                else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Brush.verticalGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // 选中图标
        AnimatedVisibility(
            visible = !isShowMenu && model.isSelected,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            CommonIcon(
                resId = R.mipmap.ic_xuanzhong,
                size = 30.dp,
                tint = if (iconDark) Color.White else Color.Black
            )
        }

        // 长按菜单
        AnimatedVisibility(
            visible = isShowMenu,
            enter = scaleIn(tween(200)) + fadeIn(tween(200)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (model.supportEdit) {
                    MenuButton(text = "编辑", onClick = onEdit)
                }
                MenuButton(
                    text = "预览",
                    onClick = onPreview,
                    modifier = Modifier.padding(top = if (model.supportEdit) 12.dp else 0.dp)
                )
                if (model.supportEdit) {
                    MenuButton(
                        text = "删除",
                        onClick = onRemove,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 添加天气背景按钮（+ 号）
 */
@Composable
private fun WeatherBgAddItem(
    modifier: Modifier = Modifier,
    itemAspectRatio: Float,
    onClick: () -> Unit
) {
    val borderColor = colorResource(R.color.black).copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .aspectRatio(itemAspectRatio)
            .bounceClick(scalePressed = 0.95f, onClick = onClick)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 竖线
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(borderColor)
        )
        // 横线
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(borderColor)
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .bounceClick(scalePressed = 0.95f, onClick = onClick)
            .clip(CircleShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            .background(Color.White.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = text,
            fontSize = 15.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

private fun calSimilarity(c1: Color, c2: Color): Float {
    val dr = c1.red - c2.red
    val dg = c1.green - c2.green
    val db = c1.blue - c2.blue
    val dist = kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
    val maxDist = kotlin.math.sqrt(3.0)
    return (1.0 - dist / maxDist).toFloat()
}

private fun weatherTypeTitle(weatherType: String): String {
    return when (weatherType) {
        "CLEAR" -> "晴天"
        "PARTLY_CLOUDY" -> "多云"
        "CLOUDY" -> "阴"
        "LIGHT_HAZE" -> "轻度雾霾、中度雾霾"
        "HEAVY_HAZE" -> "重度雾霾"
        "LIGHT_RAIN" -> "小雨"
        "MODERATE_RAIN" -> "中雨、大雨、暴雨"
        "FOG" -> "雾"
        "LIGHT_SNOW" -> "小雪、中雪、大雪、暴雪"
        "DUST" -> "浮尘、沙尘"
        "WIND" -> "大风"
        else -> ""
    }
}
