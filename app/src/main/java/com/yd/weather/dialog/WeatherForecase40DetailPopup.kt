package com.yd.weather.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.model.WeatherForecast40Data
import com.yd.weather.res.CommonIcon
import com.yd.weather.widget.WeatherForecase40Chart
import com.yd.weather.utils.Commons
import com.yd.weather.utils.WeatherIconUtils
import com.yd.weather.utils.getFormatDate
import com.yd.weather.utils.isToday
import com.yd.weather.utils.toDateString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun WeatherForecase40DetailPopup(
    forecast40: List<WeatherDetailData>?,
    forecast40Data: WeatherForecast40Data? = null,
    cityName: String? = null,
    isDark: Boolean = false,
    isWeatherHeaderDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    panelYPx: Float = 0f,
    panelWidthPx: Float = 0f,
    panelAlignment: Alignment.Horizontal = Alignment.End,
    onDismiss: () -> Unit
) {
    if (forecast40.isNullOrEmpty()) return

    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()

    var boxWidthPx by remember { mutableFloatStateOf(0f) }

    // Calendar expanded size
    val calendarExpandedHeight = 488.dp
    val calendarExpandedWidthPx = boxWidthPx - with(density) { (2 * 16).dp.toPx() }

    // Animation: 0 = panel position/size, 1 = expanded/centered
    val animProgress = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val titleBarAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    // Generate page data
    val pageData = remember(forecast40) { generatePageData(forecast40) }
    val calendarPagerState = rememberPagerState(pageCount = { pageData.size })
    var currentSelectedItem by remember {
        mutableStateOf(
            forecast40.firstOrNull { isToday(it.date) } ?: forecast40.firstOrNull()
        )
    }

    // Sync calendar pager when selection changes from chart
    val onChartItemSelected: (WeatherDetailData) -> Unit = { item ->
        currentSelectedItem = item
        val pageIndex = pageData.indexOfFirst { page -> page.any { it.date == item.date } }
        if (pageIndex >= 0) {
            scope.launch { calendarPagerState.animateScrollToPage(pageIndex) }
        }
    }

    // Enter animation
    LaunchedEffect(Unit) {
        delay(16)
        launch { contentAlpha.animateTo(1f, tween(200)) }
        animProgress.animateTo(1f, tween(200))
        titleBarAlpha.animateTo(1f, tween(200))
    }

    val exit: () -> Unit = {
        scope.launch {
            titleBarAlpha.animateTo(0f, tween(200))
            launch { animProgress.animateTo(0f, tween(200)) }
            contentAlpha.animateTo(0f, tween(200))
            onDismiss()
        }
    }

    // Animated calendar size
    val panelWidthDp = with(density) { panelWidthPx.toDp() }
    val calendarWidthDp = with(density) { calendarExpandedWidthPx.toDp() }
    val animatedCalendarWidth = panelWidthDp + (calendarWidthDp - panelWidthDp) * animProgress.value
    val panelHeightDp = Constants.ITEM_OBSERVE_PANEL_HEIGHT.dp
    val animatedCalendarHeight =
        panelHeightDp + (calendarExpandedHeight - panelHeightDp) * animProgress.value

    // Animated position: from panel Y to top+statusBar+48+12
    val titleBarHeightPx = with(density) { 48.dp.toPx() }
    val topPaddingPx = with(density) { 12.dp.toPx() }
    val startMarginTop = panelYPx - statusBarHeightPx - titleBarHeightPx - topPaddingPx
    val animatedMarginTop = startMarginTop * (1f - animProgress.value)

    // Calendar alignment: panel position → Start
    val isStartAligned = panelAlignment == Alignment.Start

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(focusable = true),
        onDismissRequest = exit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    boxWidthPx = it.width.toFloat()
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = exit
                )
        ) {
            // Title bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp)
                    .alpha(titleBarAlpha.value)
            ) {
                AppText(
                    modifier = Modifier.align(Alignment.Center),
                    text = cityName ?: "",
                    fontSize = 20.sp,
                    color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                    fontWeight = FontWeight.Bold
                )
                CommonIcon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .alphaClick(onClick = exit)
                        .padding(horizontal = 16.dp),
                    resId = R.mipmap.ic_close_icon1,
                    size = 22.dp,
                    tint = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black)
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            ) {
                // Calendar panel
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, animatedMarginTop.toInt()) }
                        .then(
                            if (isStartAligned) Modifier else Modifier.align(Alignment.End)
                        )
                        .size(animatedCalendarWidth, animatedCalendarHeight)
                        .alpha(contentAlpha.value)
                        .background(
                            colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(
                                alpha = panelOpacity
                            ),
                            shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
                        )
                        .clip(RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp))
                ) {
                    CalendarContent(
                        pageData = pageData,
                        pagerState = calendarPagerState,
                        currentSelectedItem = currentSelectedItem,
                        isDark = isDark,
                        panelOpacity = panelOpacity,
                        onItemSelected = { currentSelectedItem = it }
                    )
                }

                VerticalSpace(height = 12.dp)

                // Chart panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(288.dp)
                        .alpha(titleBarAlpha.value)
                        .background(
                            colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(
                                alpha = panelOpacity
                            ),
                            shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
                        )
                        .clip(RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)),
                    horizontalAlignment = Alignment.Start
                ) {
                    VerticalSpace(height = 10.dp)
                    AppText(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "40日天气趋势",
                        fontSize = 18.sp,
                        color = colorResource(R.color.color_white),
                        fontWeight = FontWeight.Bold
                    )
                    VerticalSpace(height = 12.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(1f))
                        AppText(
                            text = "${forecast40Data?.downDays ?: 0}",
                            fontSize = 18.sp,
                            color = colorResource(R.color.color_white)
                        )
                        AppText(
                            text = "天降温",
                            fontSize = 13.sp,
                            color = colorResource(R.color.color_white).copy(alpha = 0.6f)
                        )
                        HorizontalSpace(width = 4.dp)
                        AppText(
                            text = "${forecast40Data?.upDays ?: 0}",
                            fontSize = 18.sp,
                            color = colorResource(R.color.color_white)
                        )
                        AppText(
                            text = "天升温",
                            fontSize = 13.sp,
                            color = colorResource(R.color.color_white).copy(alpha = 0.6f)
                        )
                        HorizontalSpace(width = 4.dp)
                        AppText(
                            text = "${forecast40Data?.rainDays ?: 0}",
                            fontSize = 18.sp,
                            color = colorResource(R.color.color_white)
                        )
                        AppText(
                            text = "天有降水",
                            fontSize = 13.sp,
                            color = colorResource(R.color.color_white).copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    WeatherForecase40Chart(
                        forecast40 = forecast40,
                        currentSelectedItem = currentSelectedItem,
                        onItemSelected = onChartItemSelected
                    )
                }

                VerticalSpace(height = 12.dp)
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun CalendarContent(
    pageData: List<List<WeatherDetailData>>,
    pagerState: PagerState,
    currentSelectedItem: WeatherDetailData?,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onItemSelected: (WeatherDetailData) -> Unit
) {
    if (pageData.isEmpty()) return

    Column(modifier = Modifier.fillMaxSize()) {
        // Week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                AppText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    text = day,
                    fontSize = 12.sp,
                    color = colorResource(R.color.color_white),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            CalendarPage(
                items = pageData[page],
                currentSelectedItem = currentSelectedItem,
                isDark = isDark,
                panelOpacity = panelOpacity,
                onItemSelected = onItemSelected
            )
        }

        // Page indicator
        if (pageData.size >= 2) {
            WrapRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageData.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = colorResource(R.color.color_white)
                        .copy(alpha = if (isSelected) 1f else 0.5f)
                    if (iteration > 0) HorizontalSpace(width = 3.dp)
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

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colorResource(R.color.color_white).copy(alpha = 0.2f))
        )

        // Bottom info
        if (currentSelectedItem != null) {
            val date = getFormatDate(currentSelectedItem.date ?: "")
            val dateStr = date.toDateString("MM月dd日")
            val weekDay = getWeekDayStr(date)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    AppText(
                        text = dateStr,
                        fontSize = 18.sp,
                        color = colorResource(R.color.color_white)
                    )
                    VerticalSpace(height = 8.dp)
                    AppText(
                        text = weekDay,
                        fontSize = 15.sp,
                        color = colorResource(R.color.color_white)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!currentSelectedItem.aqiLevelName.isNullOrEmpty()) {
                            AppText(
                                modifier = Modifier
                                    .background(
                                        colorResource(
                                            Commons.getAqiColor(
                                                currentSelectedItem.aqi
                                            )
                                        ).copy(alpha = 0.48f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                text = currentSelectedItem.aqiLevelName,
                                fontSize = 11.sp,
                                color = colorResource(R.color.color_white)
                            )
                            HorizontalSpace(width = 4.dp)
                        }
                        AppText(
                            text = "${currentSelectedItem.high}/${currentSelectedItem.low}°",
                            fontSize = 18.sp,
                            color = colorResource(R.color.color_white)
                        )
                    }
                    VerticalSpace(height = 8.dp)
                    AppText(
                        text = "${currentSelectedItem.day?.wthr ?: ""} ${currentSelectedItem.day?.wd ?: ""}${currentSelectedItem.day?.wp ?: ""}",
                        fontSize = 15.sp,
                        color = colorResource(R.color.color_white)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarPage(
    items: List<WeatherDetailData>,
    currentSelectedItem: WeatherDetailData?,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onItemSelected: (WeatherDetailData) -> Unit
) {
    // 7 columns x 4 rows grid
    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < items.size) {
                        CalendarDateItem(
                            modifier = Modifier.weight(1f),
                            item = items[index],
                            preItem = if (index > 0) items[index - 1] else null,
                            isSelected = currentSelectedItem?.date == items[index].date,
                            isEnabled = items[index].day != null,
                            isDark = isDark,
                            panelOpacity = panelOpacity,
                            onItemSelected = onItemSelected
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDateItem(
    modifier: Modifier = Modifier,
    item: WeatherDetailData,
    preItem: WeatherDetailData?,
    isSelected: Boolean,
    isEnabled: Boolean,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onItemSelected: (WeatherDetailData) -> Unit
) {
    val isTodayItem = isToday(item.date)
    val isRain = item.day?.thirdType?.let {
        it == "LIGHT_RAIN" || it == "MODERATE_RAIN" || it == "HEAVY_RAIN" || it == "STORM_RAIN"
    } ?: false

    Box(
        modifier = modifier
            .then(
                if (isEnabled) Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onItemSelected(item) } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Selection background (inverse color of panel bg)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(2.dp)
                    .background(
                        colorResource(if (isDark) R.color.color_black else R.color.color_white)
                            .copy(alpha = panelOpacity),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Date text
            val dateText = if (isTodayItem) "今天" else getDateLabel(item, preItem)
            AppText(
                text = dateText,
                fontSize = 15.sp,
                color = colorResource(R.color.color_white).copy(alpha = if (isEnabled) 1f else 0.6f),
                fontWeight = if (isTodayItem) FontWeight.Bold else null
            )
            VerticalSpace(height = 4.dp)

            // Weather icon
            if (isEnabled) {
                CommonIcon(
                    resId = WeatherIconUtils.getWeatherIconByType(
                        item.day?.type ?: -1,
                        item.day?.thirdType ?: "",
                        false
                    ),
                    size = 24.dp,
                    tint = Color.Unspecified
                )
            } else {
                Spacer(Modifier.size(24.dp))
            }

            Spacer(Modifier.weight(1f))

            // Rain indicator
            if (isRain) {
                AppText(
                    modifier = Modifier
                        .background(
                            colorResource(R.color.color_0da8ff),
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    text = "降水",
                    fontSize = 13.sp,
                    color = colorResource(R.color.color_white)
                )
            } else {
                Spacer(Modifier.height(25.dp))
            }
            VerticalSpace(height = 4.dp)
        }
    }
}

private fun getDateLabel(item: WeatherDetailData, preItem: WeatherDetailData?): String {
    val date = getFormatDate(item.date ?: "")
    val day = date.toDateString("dd")
    if (preItem != null) {
        val preDate = getFormatDate(preItem.date ?: "")
        val cal = Calendar.getInstance().apply { time = date }
        val preCal = Calendar.getInstance().apply { time = preDate }
        if (cal.get(Calendar.MONTH) != preCal.get(Calendar.MONTH)) {
            return "${cal.get(Calendar.MONTH) + 1}月"
        }
    }
    return day
}

private fun getWeekDayStr(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "星期日"
        Calendar.MONDAY -> "星期一"
        Calendar.TUESDAY -> "星期二"
        Calendar.WEDNESDAY -> "星期三"
        Calendar.THURSDAY -> "星期四"
        Calendar.FRIDAY -> "星期五"
        Calendar.SATURDAY -> "星期六"
        else -> ""
    }
}

private fun generatePageData(forecast40: List<WeatherDetailData>): List<List<WeatherDetailData>> {
    val temp = forecast40.toMutableList()
    if (temp.isEmpty()) return emptyList()

    val firstDate = getFormatDate(temp.first().date ?: "")
    val lastDate = getFormatDate(temp.last().date ?: "")
    val cal = Calendar.getInstance().apply { time = firstDate }
    val firstWeekday = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY // 0=Sun

    val pages = mutableListOf<List<WeatherDetailData>>()
    var trailingIndex = 1

    for (page in 0 until 2) {
        val list = mutableListOf<WeatherDetailData>()
        for (j in 0 until 28) {
            if (page == 0) {
                if (j < firstWeekday) {
                    val placeholderCal = Calendar.getInstance().apply {
                        time = firstDate
                        add(Calendar.DATE, -(firstWeekday - j))
                    }
                    list.add(WeatherDetailData(date = placeholderCal.time.toDateString("yyyyMMdd")))
                } else if (temp.isNotEmpty()) {
                    list.add(temp.removeAt(0))
                }
            } else {
                if (temp.isNotEmpty()) {
                    list.add(temp.removeAt(0))
                } else {
                    val placeholderCal = Calendar.getInstance().apply {
                        time = lastDate
                        add(Calendar.DATE, trailingIndex)
                    }
                    list.add(WeatherDetailData(date = placeholderCal.time.toDateString("yyyyMMdd")))
                    trailingIndex++
                }
            }
        }
        pages.add(list)
    }

    return pages
}
