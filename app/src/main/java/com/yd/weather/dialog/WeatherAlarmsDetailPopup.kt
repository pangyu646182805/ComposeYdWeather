package com.yd.weather.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherAlarmsData
import com.yd.weather.utils.getFormatDate
import com.yd.weather.utils.getToday
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WeatherAlarmsDetailPopup(
    alarms: List<WeatherAlarmsData>?,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    panelYPx: Float = 0f,
    onDismiss: () -> Unit
) {
    if (alarms.isNullOrEmpty()) return

    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()

    val panelHeightPx = with(density) {
        Constants.WEATHER_ALARM_PANEL_HEIGHT.dp.toPx()
    }

    var boxHeightPx by remember { mutableFloatStateOf(0f) }
    var measuredContentHeightPx by remember { mutableFloatStateOf(0f) }

    // Animation progress: 0 = at panel position, 1 = centered
    val animProgress = remember { Animatable(0f) }
    val panelAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    // Enter animation
    LaunchedEffect(Unit) {
        delay(16)
        launch { panelAlpha.animateTo(1f, tween(200)) }
        animProgress.animateTo(1f, tween(200))
    }

    val exit: () -> Unit = {
        scope.launch {
            launch { animProgress.animateTo(0f, tween(200)) }
            panelAlpha.animateTo(0f, tween(200))
            onDismiss()
        }
    }

    // Animated height: from panel height to measured content height
    val targetHeightPx = if (measuredContentHeightPx > 0f) measuredContentHeightPx else panelHeightPx
    val animatedHeightPx = panelHeightPx + (targetHeightPx - panelHeightPx) * animProgress.value

    val startOffsetY = if (boxHeightPx > 0f) {
        (panelYPx - statusBarHeightPx) - (boxHeightPx - panelHeightPx) / 2f
    } else 0f
    val currentOffsetY = startOffsetY * (1f - animProgress.value)

    val pagerState = rememberPagerState(pageCount = { alarms.size })

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(focusable = true),
        onDismissRequest = exit
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, currentOffsetY.toInt()) }
                    .alpha(panelAlpha.value)
                    .padding(horizontal = Constants.ITEM_PANEL_MARGIN.dp)
                    .height(with(density) { animatedHeightPx.toDp() })
                    .background(
                        colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(
                            alpha = panelOpacity
                        ),
                        shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
                    )
                    .clip(RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp))
                    .clipToBounds()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .wrapContentHeight(unbounded = true)
                        .onSizeChanged { measuredContentHeightPx = it.height.toFloat() },
                    pageSpacing = 16.dp
                ) { page ->
                    DetailAlarmItem(alarms[page])
                }
                if (alarms.size >= 2) {
                    WrapRow(modifier = Modifier.padding(bottom = 10.dp)) {
                        repeat(alarms.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val color = colorResource(R.color.color_white)
                                .copy(alpha = if (isSelected) 1f else 0.5f)
                            if (iteration > 0) {
                                HorizontalSpace(width = 3.dp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 6.dp else 3.dp)
                                    .height(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailAlarmItem(item: WeatherAlarmsData) {
    val date = getFormatDate(item.pubTime ?: "")
    val diff = getToday().time - date.time
    val fewHours = (diff / 1000.0 / 60 / 60).roundToLong()
    var pubTimeDesc = "${fewHours}小时前更新"
    if (fewHours <= 0) {
        val fewMinutes = (diff / 1000.0 / 60).roundToLong()
        pubTimeDesc = "${fewMinutes}分钟前更新"
        if (fewMinutes <= 0) {
            val fewMills = (diff / 1000.0).roundToLong()
            pubTimeDesc = "${fewMills}秒前更新"
        }
    }

    AppColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        VerticalSpace(height = 12.dp)
        AppText(
            text = item.shortTitle ?: "",
            fontSize = 16.sp,
            color = colorResource(R.color.color_white),
            fontWeight = FontWeight.Bold
        )
        VerticalSpace(height = 6.dp)
        AppText(
            text = pubTimeDesc,
            fontSize = 13.sp,
            color = colorResource(R.color.color_white)
        )
        VerticalSpace(height = 8.dp)
        AppText(
            text = item.desc ?: "",
            fontSize = 14.sp,
            color = colorResource(R.color.color_white)
        )
        VerticalSpace(height = 28.dp)
    }
}
