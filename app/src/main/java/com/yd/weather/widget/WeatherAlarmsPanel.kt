package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.dialog.WeatherAlarmsDetailPopup
import com.yd.weather.model.WeatherAlarmsData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.getFormatDate
import com.yd.weather.utils.getToday
import kotlin.math.roundToLong

@Composable
fun WeatherAlarmsPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    showHideWeatherContent: ((Boolean) -> Unit)? = null
) {
    var showDetailPopup by remember { mutableStateOf(false) }
    var panelYPx by remember { mutableFloatStateOf(0f) }
    val weatherData = item.weatherData

    if (showDetailPopup) {
        WeatherAlarmsDetailPopup(
            alarms = weatherData?.alarms,
            isDark = isDark,
            panelOpacity = panelOpacity,
            panelYPx = panelYPx,
            onDismiss = {
                showDetailPopup = false
                showHideWeatherContent?.invoke(true)
            }
        )
    }

    WeatherStickyPanel(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                panelYPx = coordinates.positionOnScreen().y
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showHideWeatherContent?.invoke(false)
                showDetailPopup = true
            },
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = Constants.WEATHER_ALARM_PANEL_HEIGHT,
        stickyTitle = "极端天气",
        supportTitleOpacity = true
    ) { offsetPx, titleOpacity, offset ->
        val timeOpacity = (1 - offset / 28).coerceIn(0f, 1f)
        Swiper(
            alarms = weatherData?.alarms,
            offsetPx = offsetPx,
            titleOpacity = titleOpacity,
            timeOpacity = timeOpacity
        )
    }
}

@Composable
internal fun Swiper(
    modifier: Modifier = Modifier,
    alarms: List<WeatherAlarmsData>?,
    offsetPx: Float = 0f,
    titleOpacity: Float = 1F,
    timeOpacity: Float = 1F,
    isDark: Boolean = false,
    panelOpacity: Float = 0f
) {
    if (alarms.isNullOrEmpty()) return
    val pagerState = rememberPagerState(pageCount = { alarms.size })

    val bgModifier = if (panelOpacity > 0f) Modifier.background(
        colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
        shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
    ) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Constants.WEATHER_ALARM_PANEL_HEIGHT.dp)
            .then(bgModifier)
            .clip(if (offsetPx != 0f) WeatherPanelClip(offsetPx) else RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .height(Constants.WEATHER_ALARM_PANEL_HEIGHT.dp),
            pageSpacing = 16.dp
        ) { page ->
            AlarmItem(alarms[page], titleOpacity = titleOpacity, timeOpacity = timeOpacity)
        }
        if (alarms.size >= 2)
            WrapRow(modifier = Modifier.padding(bottom = 10.dp)) {
                repeat(alarms.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color =
                        colorResource(R.color.color_white).copy(alpha = if (isSelected) 1f else 0.5f)
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

@Composable
internal fun AlarmItem(item: WeatherAlarmsData, titleOpacity: Float = 1F, timeOpacity: Float = 1F) {
    var pubTimeDesc: String
    val date = getFormatDate(item.pubTime ?: "")
    val diff = getToday().time - date.time
    val fewHours = (diff / 1000.0 / 60 / 60).roundToLong()
    pubTimeDesc = "${fewHours}小时前更新"
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
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        VerticalSpace(height = 12.dp)
        AppText(
            modifier = Modifier.alpha(titleOpacity),
            text = item.shortTitle ?: "",
            fontSize = 16.sp,
            color = colorResource(R.color.color_white),
            fontWeight = FontWeight.Bold
        )
        VerticalSpace(height = 6.dp)
        AppText(
            modifier = Modifier.alpha(timeOpacity),
            text = pubTimeDesc,
            fontSize = 13.sp,
            color = colorResource(R.color.color_white)
        )
        VerticalSpace(height = 8.dp)
        AppText(
            text = item.desc ?: "",
            fontSize = 14.sp,
            color = colorResource(R.color.color_white),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
