package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherAlarmsData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.WeatherPanelClip
import com.yd.weather.utils.getFormatDate
import com.yd.weather.utils.getToday
import java.nio.file.WatchEvent
import kotlin.math.roundToLong

@Composable
fun WeatherAlarmsPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val density = LocalDensity.current
    val weatherData = item.weatherData
    val offset = when {
        index + 1 > firstVisibleItemIndex -> 0f
        index + 1 == firstVisibleItemIndex -> firstItemOffset
        else -> Constants.WEATHER_ALARM_PANEL_HEIGHT.toFloat()
    }
    val percent =
        ((offset - (Constants.WEATHER_ALARM_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT)) / Constants.ITEM_STICKY_HEIGHT)
            .coerceIn(0f, 1f)
    val contentOpacity = 1 - percent
    var stickyTranslateY =
        if (offset > Constants.WEATHER_ALARM_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT)
            (Constants.WEATHER_ALARM_PANEL_HEIGHT - Constants.ITEM_STICKY_HEIGHT).toFloat()
        else
            offset
    stickyTranslateY += percent * Constants.ITEM_STICKY_HEIGHT * 0.5f
    val titleOpacity = (1 - offset / 12).coerceIn(0f, 1f)
    val timeOpacity = (1 - offset / 28).coerceIn(0f, 1f)
    // println("index = $index offset = $offset stickyTranslateY = $stickyTranslateY")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.WEATHER_ALARM_PANEL_HEIGHT.dp)
            .alpha(contentOpacity)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
            )
    ) {
        AppText(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.ITEM_STICKY_HEIGHT.dp)
                .graphicsLayer(
                    alpha = 1 - timeOpacity,
                    translationY = with(density) { stickyTranslateY.dp.toPx() })
                .padding(start = Constants.ITEM_PANEL_MARGIN.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            text = "极端天气",
            fontSize = 12.sp,
            color = colorResource(R.color.color_white).copy(alpha = 0.6f),
            textAlign = TextAlign.Start
        )
        Swiper(
            alarms = weatherData?.alarms,
            offsetPx = with(density) {
                (offset + Constants.ITEM_STICKY_HEIGHT * (offset / Constants.ITEM_STICKY_HEIGHT).coerceIn(
                    0f, 1f
                )).dp.toPx()
            },
            titleOpacity = titleOpacity,
            timeOpacity = timeOpacity
        )
    }
}

@Composable
internal fun Swiper(
    alarms: List<WeatherAlarmsData>?,
    offsetPx: Float = 0f,
    titleOpacity: Float = 1F,
    timeOpacity: Float = 1F
) {
    if (alarms.isNullOrEmpty()) return
    val pagerState = rememberPagerState(pageCount = { alarms.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.WEATHER_ALARM_PANEL_HEIGHT.dp)
            .clip(WeatherPanelClip(offsetPx)),
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
