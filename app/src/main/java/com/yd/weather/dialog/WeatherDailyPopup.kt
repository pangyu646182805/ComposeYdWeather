package com.yd.weather.dialog

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.Commons
import com.yd.weather.utils.WeatherIconUtils
import com.yd.weather.utils.formatDateStr
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHOW_COUNT = 3
private const val CARD_SCALE = 0.95f
private const val CARD_RADIUS_DP = 12

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WeatherDailyPopup(
    initialIndex: Int = 0,
    forecast15: List<WeatherDetailData>?,
    weatherBg: List<Color>,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    onDismiss: () -> Unit
) {
    if (forecast15.isNullOrEmpty()) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    val paddingDp = 16f
    // Flutter: ratio = (screenWidth - 2*16 - 24) / itemHeight
    // 卡片的基础宽高（最前面的卡片尺寸）
    val cardHeightDp = 204f
    val cardWidthDp = screenWidthDp - 2 * paddingDp - 24f
    val cardWidthPx = with(density) { cardWidthDp.dp.toPx() }
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }

    // 堆叠间距（Flutter: offset = (totalWidth - paddingLeft - firstWidth - paddingRight) / (showCount-1)）
    val stackOffsetDp = (screenWidthDp - paddingDp - cardWidthDp - paddingDp) / (SHOW_COUNT - 1)

    var currentIndex by remember {
        mutableIntStateOf(
            initialIndex.coerceIn(
                0,
                forecast15.size - 1
            )
        )
    }
    val dragAnimatable = remember { Animatable(0f) }
    val dragProgress = dragAnimatable.value
    // outside 卡片的独立位置（right 边缘的 px 值，0=刚好隐藏，frontRight=完全到位）
    val outsideAnimatable = remember { Animatable(0f) }
    val outsideRightPx = outsideAnimatable.value
    var isAnimating by remember { mutableStateOf(false) }

    // 动画
    val contentAlpha = remember { Animatable(0f) }
    val enterOffsets = remember { List(SHOW_COUNT) { Animatable(1f) } }

    LaunchedEffect(Unit) {
        delay(16)
        contentAlpha.animateTo(1f, tween(200))
        // 卡片依次从右侧滑入（最后面的先进，最前面的最后进）
        for (i in 0 until SHOW_COUNT) {
            val cardIdx = SHOW_COUNT - 1 - i
            launch {
                delay(50L * i)
                enterOffsets[cardIdx].animateTo(
                    0f,
                    tween(200, easing = androidx.compose.animation.core.EaseOutBack)
                )
            }
        }
    }

    val exit: () -> Unit = {
        scope.launch {
            for (i in 0 until SHOW_COUNT) {
                launch {
                    delay(50L * i)
                    enterOffsets[i].animateTo(1f, tween(200))
                }
            }
            delay(250)
            contentAlpha.animateTo(0f, tween(150))
            onDismiss()
        }
    }

    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { backEvent ->
                val p = backEvent.progress
                // 卡片跟随手势进度水平滑出
                for (i in 0 until SHOW_COUNT) {
                    enterOffsets.getOrNull(i)?.snapTo(p)
                }
            }
            // 手势完成 → 快速滑出 + 关闭
            coroutineScope {
                for (i in 0 until SHOW_COUNT) {
                    launch {
                        delay(50L * i)
                        enterOffsets[i].animateTo(1f, tween(150))
                    }
                }
            }
            contentAlpha.animateTo(0f, tween(100))
            onDismiss()
        } catch (_: CancellationException) {
            // 手势取消 → 弹回
            coroutineScope {
                for (i in 0 until SHOW_COUNT) {
                    launch {
                        enterOffsets[i].animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                    }
                }
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
                .alpha(contentAlpha.value)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = exit
                ),
            contentAlignment = Alignment.Center
        ) {
            // 卡片堆叠区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeightDp.dp)
                    .pointerInput(Unit) {
                        // 拦截点击，不穿透到外层 dismiss
                        detectTapGestures { }
                    }
                    .pointerInput(currentIndex) {
                        var velocityTracker = 0f
                        detectHorizontalDragGestures(
                            onDragStart = {
                                if (isAnimating) return@detectHorizontalDragGestures
                                velocityTracker = 0f
                            },
                            onDragEnd = {
                                if (isAnimating) return@detectHorizontalDragGestures
                                isAnimating = true
                                val currentProgress = dragAnimatable.value
                                val outsideRight = outsideAnimatable.value
                                val frontRightPx =
                                    with(density) { (screenWidthDp - paddingDp).dp.toPx() }
                                val flingThreshold = 1000f
                                scope.launch {
                                    if (outsideRight > 0 && currentIndex > 0) {
                                        // 向右滑模式
                                        if (outsideRight > cardWidthPx * 0.25f || velocityTracker > flingThreshold) {
                                            // 确认：outside 卡片动画到前景位置，当前卡片后退
                                            launch {
                                                outsideAnimatable.animateTo(
                                                    frontRightPx,
                                                    tween(
                                                        400,
                                                        easing = androidx.compose.animation.core.EaseOutBack
                                                    )
                                                )
                                            }
                                            launch {
                                                dragAnimatable.animateTo(
                                                    1f,
                                                    tween(
                                                        400,
                                                        easing = androidx.compose.animation.core.EaseOutBack
                                                    )
                                                )
                                            }
                                            delay(400)
                                            currentIndex--
                                            dragAnimatable.snapTo(0f)
                                            outsideAnimatable.snapTo(0f)
                                        } else {
                                            // 取消：outside 卡片退回，当前卡片恢复
                                            launch {
                                                outsideAnimatable.animateTo(
                                                    0f,
                                                    tween(
                                                        400,
                                                        easing = androidx.compose.animation.core.EaseOutBack
                                                    )
                                                )
                                            }
                                            launch {
                                                dragAnimatable.animateTo(
                                                    0f,
                                                    tween(
                                                        400,
                                                        easing = androidx.compose.animation.core.EaseOutBack
                                                    )
                                                )
                                            }
                                            delay(400)
                                        }
                                    } else if (currentProgress < -0.25f || velocityTracker < -flingThreshold) {
                                        if (currentIndex < forecast15.size - 1) {
                                            // 向左滑确认
                                            dragAnimatable.animateTo(
                                                -1f,
                                                tween(
                                                    400,
                                                    easing = androidx.compose.animation.core.EaseOutBack
                                                )
                                            )
                                            currentIndex++
                                            dragAnimatable.snapTo(0f)
                                        } else {
                                            dragAnimatable.animateTo(
                                                0f,
                                                tween(
                                                    400,
                                                    easing = androidx.compose.animation.core.EaseOutBack
                                                )
                                            )
                                        }
                                    } else {
                                        // 弹回
                                        dragAnimatable.animateTo(
                                            0f,
                                            tween(
                                                400,
                                                easing = androidx.compose.animation.core.EaseOutBack
                                            )
                                        )
                                    }
                                    outsideAnimatable.snapTo(0f)
                                    isAnimating = false
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    launch { dragAnimatable.animateTo(0f, tween(400)) }
                                    launch { outsideAnimatable.animateTo(0f, tween(400)) }
                                    isAnimating = false
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (isAnimating) return@detectHorizontalDragGestures
                                velocityTracker = dragAmount // 简化的速度追踪
                                val outsideRight = outsideAnimatable.value
                                if (outsideRight > 0 || (dragAmount > 0 && currentIndex > 0 && dragAnimatable.value >= 0)) {
                                    // 向右滑模式：outside 卡片跟手平移
                                    val newRight = (outsideRight + dragAmount).coerceIn(
                                        0f, with(density) { (screenWidthDp - paddingDp).dp.toPx() }
                                    )
                                    scope.launch { outsideAnimatable.snapTo(newRight) }
                                    // 当前卡片按 outside 的进度后退
                                    val frontRightPx =
                                        with(density) { (screenWidthDp - paddingDp).dp.toPx() }
                                    val percent = (newRight / frontRightPx).coerceIn(0f, 1f)
                                    scope.launch { dragAnimatable.snapTo(percent) }
                                } else {
                                    // 向左滑模式
                                    val newValue =
                                        (dragAnimatable.value + dragAmount / cardWidthPx).coerceIn(
                                            -1f,
                                            0f
                                        )
                                    scope.launch { dragAnimatable.snapTo(newValue) }
                                }
                            }
                        )
                    }
            ) {
                // 辅助函数：计算 posInStack 对应的基础属性
                fun calcCard(pos: Int): FloatArray {
                    val ni = SHOW_COUNT - 1 - pos
                    val sc = CARD_SCALE.pow(pos)
                    val cw = cardWidthDp * sc
                    val ch = cardHeightDp * sc
                    val cl = screenWidthDp - paddingDp - cw - stackOffsetDp * ni
                    val ct = (cardHeightDp - ch) / 2f
                    val ca = if (pos == 0) 1f else (ni + 1).toFloat() / (SHOW_COUNT + 1)
                    return floatArrayOf(cl, ct, cw, ch, ca) // left, top, w, h, alpha
                }

                // 当前显示的卡片索引列表
                val showingDataIndices = (0 until SHOW_COUNT)
                    .map { currentIndex + it }
                    .filter { it in forecast15.indices }

                // 向右滑时，显示 outside 卡片（前一张从左侧滑入）
                val outsideIndex =
                    if (outsideRightPx > 0 && currentIndex > 0) currentIndex - 1 else -1

                // 构建绘制列表（从后往前画）
                data class CardInfo(val dataIndex: Int, val posInStack: Int, val isOutside: Boolean)

                val cardsToDraw = mutableListOf<CardInfo>()
                for (di in showingDataIndices.reversed()) {
                    cardsToDraw.add(CardInfo(di, di - currentIndex, false))
                }
                if (outsideIndex >= 0) {
                    cardsToDraw.add(CardInfo(outsideIndex, -1, true))
                }

                for (card in cardsToDraw) {
                    val posInStack = card.posInStack
                    val base = calcCard(posInStack.coerceAtLeast(0))

                    val left: Float
                    val top: Float
                    val w: Float
                    val h: Float
                    val alpha: Float

                    if (card.isOutside) {
                        // outside 卡片：跟手平移，用 outsideRightPx 定位（和 Flutter 一样直接 translate）
                        val frontCard = calcCard(0)
                        w = frontCard[2]
                        h = frontCard[3]
                        top = frontCard[1]
                        // right = outsideRightPx → left = right - w（dp 坐标）
                        val outsideRightDp = with(density) { outsideRightPx.toDp().value }
                        left = outsideRightDp - w
                        alpha = 1f
                    } else if (outsideRightPx > 0 && currentIndex > 0) {
                        // 向右拖：当前所有卡片向后退（posInStack → posInStack+1）
                        val rp = dragProgress.coerceIn(0f, 1f)
                        val nextCard = calcCard(posInStack + 1)
                        left = base[0] + (nextCard[0] - base[0]) * rp
                        top = base[1] + (nextCard[1] - base[1]) * rp
                        w = base[2] + (nextCard[2] - base[2]) * rp
                        h = base[3] + (nextCard[3] - base[3]) * rp
                        alpha = base[4] + (nextCard[4] - base[4]) * rp
                    } else if (dragProgress < 0 && posInStack > 0) {
                        // 向左拖：后面的卡片向前进（posInStack → posInStack-1）
                        val lp = (-dragProgress).coerceIn(0f, 1f)
                        val prevCard = calcCard(posInStack - 1)
                        left = base[0] + (prevCard[0] - base[0]) * lp
                        top = base[1] + (prevCard[1] - base[1]) * lp
                        w = base[2] + (prevCard[2] - base[2]) * lp
                        h = base[3] + (prevCard[3] - base[3]) * lp
                        alpha = base[4] + (prevCard[4] - base[4]) * lp
                    } else {
                        left = base[0]; top = base[1]; w = base[2]; h = base[3]; alpha = base[4]
                    }

                    // 前景卡片跟随向左拖拽（向右拖时不移动前景，由 outside 卡片覆盖）
                    val leftPxBase = with(density) { left.dp.toPx() }
                    val dragOffsetPx = if (posInStack == 0 && dragProgress < 0) {
                        dragProgress * (leftPxBase + with(density) { w.dp.toPx() })
                    } else 0f

                    // 入场动画
                    val enterOffset =
                        if (!card.isOutside) enterOffsets.getOrNull(posInStack)?.value ?: 0f else 0f
                    val enterPx = enterOffset * screenWidthPx

                    val leftPx = leftPxBase + dragOffsetPx + enterPx
                    val topPx = with(density) { top.dp.toPx() }
                    val wPx = with(density) { w.dp.toPx() }
                    val hPx = with(density) { h.dp.toPx() }

                    Layout(
                        content = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(alpha.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(CARD_RADIUS_DP.dp))
                            ) {
                                DailyCardContent(
                                    item = forecast15[card.dataIndex],
                                    weatherBg = weatherBg,
                                    isDark = isDark,
                                    panelOpacity = panelOpacity
                                )
                            }
                        },
                        measurePolicy = { measurables, _ ->
                            val wInt = wPx.roundToInt().coerceAtLeast(0)
                            val hInt = hPx.roundToInt().coerceAtLeast(0)
                            val placeable = measurables.first().measure(
                                Constraints.fixed(wInt, hInt)
                            )
                            layout(wInt, hInt) {
                                placeable.place(leftPx.roundToInt(), topPx.roundToInt())
                            }
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun DailyCardContent(
    item: WeatherDetailData,
    weatherBg: List<Color>,
    isDark: Boolean,
    panelOpacity: Float
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // 天气渐变背景（模拟 Flutter OverflowBox：全屏尺寸渐变居中，被卡片 clip 裁剪）
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(screenWidthDp)
                    .requiredHeight(screenHeightDp)
                    .background(
                        Brush.verticalGradient(
                            weatherBg.ifEmpty { listOf(Color.Gray, Color.DarkGray) }
                        )
                    )
            )
        }
        // 半透明面板叠层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    colorResource(if (isDark) R.color.color_white else R.color.color_black)
                        .copy(alpha = panelOpacity)
                )
        )
        // 内容
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpace(height = 12.dp)

            // 日期 + AQI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AppText(
                    text = formatDateStr(item.date, "MM月dd日") ?: "",
                    fontSize = 15.sp,
                    color = colorResource(R.color.color_white),
                    fontWeight = FontWeight.Medium
                )
                if (!item.aqiLevelName.isNullOrEmpty()) {
                    HorizontalSpace(width = 4.dp)
                    AppText(
                        modifier = Modifier
                            .background(
                                colorResource(Commons.getAqiColor(item.aqi)).copy(alpha = 0.48f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        text = item.aqiLevelName,
                        fontSize = 11.sp,
                        color = colorResource(R.color.color_white)
                    )
                }
            }

            VerticalSpace(height = 12.dp)

            // 白天 / 夜间
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    DayNightColumn(item = item, isNight = false)
                }
                Box(modifier = Modifier.weight(1f)) {
                    DayNightColumn(item = item, isNight = true)
                }
            }
        }
    }
}

@Composable
private fun DayNightColumn(
    item: WeatherDetailData,
    isNight: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonIcon(
            resId = WeatherIconUtils.getWeatherIconByType(
                if (isNight) item.night?.type ?: -1 else item.day?.type ?: -1,
                if (isNight) item.night?.thirdType ?: "" else item.day?.thirdType ?: "",
                isNight
            ),
            size = 32.dp,
            tint = Color.Unspecified
        )
        VerticalSpace(height = 12.dp)
        AppText(
            text = if (isNight) "夜间" else "白天",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white)
        )
        VerticalSpace(height = 6.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppText(
                text = if (isNight) item.night?.wthr ?: "" else item.day?.wthr ?: "",
                fontSize = 13.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f)
            )
            AppText(
                text = Commons.getTemp(if (isNight) item.low else item.high),
                fontSize = 13.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f)
            )
        }
        VerticalSpace(height = 6.dp)
        AppText(
            text = "${item.wd ?: ""}${item.wp ?: ""}",
            fontSize = 13.sp,
            color = colorResource(R.color.color_white).copy(alpha = 0.6f)
        )
        VerticalSpace(height = 10.dp)
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(0.5.dp)
                .background(colorResource(R.color.color_white).copy(alpha = 0.6f))
        )
        VerticalSpace(height = 10.dp)
        AppText(
            text = if (isNight) "日落 ${item.sunset ?: ""}" else "日出 ${item.sunrise ?: ""}",
            fontSize = 15.sp,
            color = colorResource(R.color.color_white)
        )
    }
}
