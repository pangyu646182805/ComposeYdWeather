package com.yd.weather.dialog

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherEnvData
import com.yd.weather.res.CommonIcon
import com.yd.weather.widget.AirQualityBar
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AirQualityDetailPopup(
    evn: WeatherEnvData?,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    panelYPx: Float = 0f,
    onQueryClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()

    // Content total approximate height (panel + gap + 2 rows of 72dp + gap)
    val contentHeightPx = with(density) {
        (Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT + 12 + 72 + 12 + 72).dp.toPx()
    }

    var boxHeightPx by remember { mutableFloatStateOf(0f) }

    // Animation progress: 0 = at panel position (TopCenter), 1 = centered
    val animProgress = remember { Animatable(0f) }
    // Panel opacity
    val panelAlpha = remember { Animatable(0f) }
    // Grid content opacity
    val contentAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    // Enter animation (aboutToAppear logic)
    LaunchedEffect(Unit) {
        delay(16)
        // Panel fades in + moves to center
        launch { panelAlpha.animateTo(1f, tween(200)) }
        launch { animProgress.animateTo(1f, tween(200)) }
        // Grid fades in after panel animation
        delay(200)
        contentAlpha.animateTo(1f, tween(200))
    }

    val exit: () -> Unit = {
        scope.launch {
            // Content fades out + panel moves back
            launch { contentAlpha.animateTo(0f, tween(200)) }
            launch { animProgress.animateTo(0f, tween(200)) }
            delay(200)
            // Panel fades out
            panelAlpha.animateTo(0f, tween(200))
            onDismiss()
        }
    }

    // Offset from center: 0 when centered, negative when at panel position above center
    val startOffsetY = if (boxHeightPx > 0f) {
        (panelYPx - statusBarHeightPx) - (boxHeightPx - contentHeightPx) / 2f
    } else 0f
    val currentOffsetY = startOffsetY * (1f - animProgress.value)

    // 预测返回手势：放在 Popup 外面（主 composition），Popup 改为非焦点模式
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { backEvent ->
                val p = backEvent.progress
                // 反向入场动画：居中(1) → 面板原位(0)
                animProgress.snapTo(1f - p)
                // 污染物网格先消失
                contentAlpha.snapTo((1f - p * 2f).coerceIn(0f, 1f))
            }
            // 手势完成 → 快速收尾 + 关闭
            coroutineScope {
                launch { contentAlpha.animateTo(0f, tween(100)) }
                launch { animProgress.animateTo(0f, tween(100)) }
            }
            panelAlpha.animateTo(0f, tween(150))
            onDismiss()
        } catch (_: CancellationException) {
            // 手势取消 → 弹回完全显示
            coroutineScope {
                launch { animProgress.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
                launch { contentAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
            }
        }
    }

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(focusable = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { boxHeightPx = it.height.toFloat() }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = exit
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, currentOffsetY.toInt()) }
                    .alpha(panelAlpha.value)
                    .padding(horizontal = Constants.ITEM_PANEL_MARGIN.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            ) {
                // Air quality panel
                AppColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.WEATHER_AIR_QUALITY_PANEL_HEIGHT.dp)
                        .background(
                            colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(
                                alpha = panelOpacity
                            ),
                            shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
                        )
                        .clip(RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    VerticalSpace(height = 12.dp)
                    WrapRow {
                        AppText(
                            text = "${evn?.aqi} - ${evn?.aqiLevelName}",
                            fontSize = 16.sp,
                            color = colorResource(R.color.color_white),
                            fontWeight = FontWeight.Medium
                        )
                        CommonIcon(
                            modifier = Modifier
                                .alphaClick(onClick = onQueryClick)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            resId = R.mipmap.ic_query_icon,
                            size = 14.dp,
                            tint = colorResource(R.color.color_white)
                        )
                    }
                    VerticalSpace(height = 12.dp)
                    AirQualityBar(barHeight = 4.dp, aqi = evn?.aqi ?: 0)
                    VerticalSpace(height = 10.dp)
                    AppText(
                        text = "当前AQI为${evn?.aqi}",
                        fontSize = 13.sp,
                        color = colorResource(R.color.color_white)
                    )
                }

                VerticalSpace(height = 12.dp)

                // Pollutant grid (3 columns x 2 rows)
                Column(
                    modifier = Modifier.alpha(contentAlpha.value),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PollutantItem(
                            Modifier.weight(1f),
                            "PM2.5",
                            null,
                            evn?.pm25 ?: 0,
                            isDark,
                            panelOpacity
                        )
                        PollutantItem(
                            Modifier.weight(1f),
                            "PM10",
                            null,
                            evn?.pm10 ?: 0,
                            isDark,
                            panelOpacity
                        )
                        PollutantItem(
                            Modifier.weight(1f),
                            "NO",
                            "2",
                            evn?.no2 ?: 0,
                            isDark,
                            panelOpacity
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PollutantItem(
                            Modifier.weight(1f),
                            "SO",
                            "2",
                            evn?.so2 ?: 0,
                            isDark,
                            panelOpacity
                        )
                        PollutantItem(
                            Modifier.weight(1f),
                            "O",
                            "3",
                            evn?.o3 ?: 0,
                            isDark,
                            panelOpacity
                        )
                        PollutantItem(
                            Modifier.weight(1f),
                            "CO",
                            null,
                            evn?.co ?: 0,
                            isDark,
                            panelOpacity
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PollutantItem(
    modifier: Modifier = Modifier,
    title: String,
    subscript: String?,
    value: Int,
    isDark: Boolean,
    panelOpacity: Float
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
            )
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            AppText(
                text = title,
                fontSize = 14.sp,
                color = colorResource(R.color.color_white),
                fontWeight = FontWeight.Medium
            )
            if (!subscript.isNullOrEmpty()) {
                AppText(
                    text = subscript,
                    fontSize = 9.sp,
                    color = colorResource(R.color.color_white),
                    fontWeight = FontWeight.Medium
                )
            }
            AppText(
                modifier = Modifier.padding(start = 2.dp),
                text = "ug/m³",
                fontSize = 11.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f)
            )
        }
        VerticalSpace(height = 12.dp)
        AppText(
            text = "$value.0",
            fontSize = 18.sp,
            color = colorResource(R.color.color_white),
            fontWeight = FontWeight.Medium
        )
    }
}
