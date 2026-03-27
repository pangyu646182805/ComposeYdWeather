package com.yd.weather.weatherbglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import com.yd.weather.model.WeatherBgModel
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.rememberElasticScrollState

@Composable
internal fun WeatherBgListRoute(
    viewModel: WeatherBgListViewModel = hiltViewModel()
) {
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
        onRemoveBg = { type, model -> viewModel.removeWeatherBg(type, model) }
    )
}

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
    onRemoveBg: (String, WeatherBgModel) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val itemAspectRatio = screenWidth.toFloat() / screenHeight.toFloat()
    // 每个 item 占列宽：(屏幕宽 - 左右 padding 40 - 两个间距 24) / 3
    val itemWidthDp = ((screenWidth - 40 - 24) / 3f).dp

    val elastic = rememberElasticScrollState(orientation = Orientation.Vertical)
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bg_color))
            .pointerInput(isShowMenu) {
                if (isShowMenu) {
                    awaitPointerEventScope {
                        awaitPointerEvent()
                        onHideMenu()
                    }
                }
            }
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
                        text = if (isShowMenu) "全部删除" else if (isNight) "夜间" else "日间",
                        color = colorResource(R.color.black),
                        fontSize = 14.sp
                    )
                }
            }
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .nestedScroll(elastic.connection)
                .graphicsLayer { translationY = elastic.overscrollOffset }
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(weatherBgMap.keys.toList(), key = { it }) { weatherType ->
                val models = weatherBgMap[weatherType] ?: emptyList()
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
                        models.forEachIndexed { index, model ->
                            WeatherBgGridItem(
                                modifier = Modifier.weight(1f),
                                model = model,
                                isNight = isNight,
                                isShowMenu = isShowMenu,
                                itemAspectRatio = itemAspectRatio,
                                onLongPress = onToggleMenu,
                                onClick = {
                                    if (isShowMenu) {
                                        onHideMenu()
                                    } else if (!model.isSelected) {
                                        onSelectBg(weatherType, model)
                                    }
                                },
                                onRemove = { onRemoveBg(weatherType, model) }
                            )
                        }
                        // 占位：不足 3 列时补空位保持对齐
                        val remainder = models.size % 3
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

@Composable
private fun WeatherBgGridItem(
    modifier: Modifier = Modifier,
    model: WeatherBgModel,
    isNight: Boolean,
    isShowMenu: Boolean,
    itemAspectRatio: Float,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = if (isNight) model.nightColors else model.colors
    val gradientColors = colors.map { Color(it) }
    val bgColor = colorResource(R.color.bg_color)
    val needBorder = gradientColors.all { calSimilarity(bgColor, it) > 0.95f }

    Box(
        modifier = modifier
            .aspectRatio(itemAspectRatio)
            .bounceClick(
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
            val iconDark =
                gradientColors.firstOrNull()?.let { (1 - it.luminance()) > 0.5f } ?: false
            CommonIcon(
                resId = R.mipmap.ic_checked_icon,
                size = 30.dp,
                tint = if (iconDark) Color.White else Color.Black
            )
        }

        // 长按菜单
        AnimatedVisibility(
            visible = isShowMenu,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (model.supportEdit) {
                    MenuButton(text = "删除", onClick = onRemove)
                }
            }
        }
    }
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .bounceClick(onClick = onClick)
            .clip(CircleShape)
            .background(Color.White),
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
