package com.yd.weather.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherIndexData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 生活指数详情气泡弹窗（参照 Flutter LifeIndexDialog）
 *
 * @param indexes 生活指数数据列表
 * @param initialIndex 初始展示的 item index
 * @param cellSizePx grid 单元格尺寸（px）
 * @param gridContentXPx grid 内容区在屏幕中的 X（px）
 * @param gridContentYPx grid 内容区在屏幕中的 Y（px，不含 sticky header）
 * @param onDismiss 关闭回调
 */
@Composable
fun LifeIndexDetailPopup(
    indexes: List<WeatherIndexData>,
    initialIndex: Int,
    cellSizePx: Float,
    gridContentXPx: Float,
    gridContentYPx: Float,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rowCount = kotlin.math.ceil(indexes.size / 3.0).toInt()

    // 当前展示的 index（内部管理 + 响应外部 initialIndex 变化）
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    // 只在外部 initialIndex 真正变化时同步（避免和内部拖拽互相打架）
    LaunchedEffect(initialIndex) {
        currentIndex = initialIndex
    }
    val currentData = indexes.getOrNull(currentIndex) ?: return

    // 整体淡入淡出
    val opacity = remember { Animatable(0f) }

    // 内容切换淡入淡出（参照 Flutter _contentOpacity）
    var contentOpacity by remember { mutableFloatStateOf(1f) }
    val animatedContentOpacity by animateFloatAsState(
        targetValue = contentOpacity,
        animationSpec = if (contentOpacity == 0f) tween(0) else tween(200),
        label = "contentOpacity"
    )

    // 用于展示的数据（淡出完成后再切换）
    var displayData by remember { mutableStateOf(currentData) }
    var displayColumn by remember { mutableIntStateOf(initialIndex % 3) }
    var displayRow by remember { mutableIntStateOf(initialIndex / 3) }

    // 弹窗卡片高度（px，用于定位）
    var cardHeightPx by remember { mutableFloatStateOf(0f) }

    // 淡入
    LaunchedEffect(Unit) {
        opacity.animateTo(1f, tween(200))
    }

    // currentIndex 变化时：位置立即更新（动画过渡），内容淡出 → 切换 → 淡入
    LaunchedEffect(currentIndex) {
        // 位置立即更新，让 animateXxxAsState 驱动动画
        displayColumn = currentIndex % 3
        displayRow = currentIndex / 3
        // 内容淡出 → 切换 → 淡入
        contentOpacity = 0f
        delay(50)
        displayData = indexes.getOrNull(currentIndex) ?: return@LaunchedEffect
        contentOpacity = 1f
    }

    // 根据屏幕触摸坐标计算 grid item index
    fun calcIndexFromScreenPosition(screenX: Float, screenY: Float): Int {
        val col = ((screenX - gridContentXPx) / cellSizePx).toInt().coerceIn(0, 2)
        val row = ((screenY - gridContentYPx) / cellSizePx).toInt().coerceIn(0, rowCount - 1)
        return (row * 3 + col).coerceIn(0, indexes.size - 1)
    }

    val exit: () -> Unit = {
        scope.launch {
            opacity.animateTo(0f, tween(200))
            onDismiss()
        }
    }

    // item 在屏幕上的位置（dp）- 使用动画过渡
    val targetItemTopDp = with(density) { (gridContentYPx + displayRow * cellSizePx).toDp() }
    val targetItemCenterXPx = gridContentXPx + displayColumn * cellSizePx + cellSizePx / 2

    // 弹窗对齐（参照 Flutter AnimatedAlign）
    val targetAlignmentBias = when (displayColumn) {
        0 -> -1f  // Start
        1 -> 0f   // Center
        else -> 1f // End
    }
    val positionSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val animatedAlignmentBias by animateFloatAsState(
        targetValue = targetAlignmentBias,
        animationSpec = positionSpring,
        label = "alignmentBias"
    )

    // 箭头 X 在 padding 内的位置（px）- 动画过渡
    val panelMarginPx = with(density) { Constants.ITEM_PANEL_MARGIN.dp.toPx() }
    val targetArrowXPx = targetItemCenterXPx - panelMarginPx
    val animatedArrowXPx by animateFloatAsState(
        targetValue = targetArrowXPx,
        animationSpec = positionSpring,
        label = "arrowX"
    )

    // 卡片顶部 Y（在 item 上方，留 12dp 间距 + 8dp 箭头高度）- 动画过渡
    val targetCardTopDp = targetItemTopDp - with(density) { cardHeightPx.toDp() } - 28.dp
    val animatedCardTopDp by animateDpAsState(
        targetValue = targetCardTopDp.coerceAtLeast(0.dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardTop"
    )
    val arrowColor = colorResource(R.color.color_white)

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(focusable = true),
        onDismissRequest = exit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(opacity.value)
        ) {
            // 透明遮罩：点击关闭 + 长按拖拽切换（合并到一个 pointerInput 避免冲突）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val longPress = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                waitForUpOrCancellation()
                            }
                            if (longPress != null) {
                                // 短按（手指已抬起）→ 关闭
                                exit()
                            } else {
                                // 长按触发 → 开始拖拽切换
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                val startIndex = calcIndexFromScreenPosition(
                                    down.position.x, down.position.y
                                )
                                if (startIndex != currentIndex && startIndex in indexes.indices) {
                                    currentIndex = startIndex
                                }
                                // 追踪拖拽
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    change.consume()
                                    val newIndex = calcIndexFromScreenPosition(
                                        change.position.x, change.position.y
                                    )
                                    if (newIndex != currentIndex && newIndex in indexes.indices) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentIndex = newIndex
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    }
            )

            // 气泡弹窗
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Constants.ITEM_PANEL_MARGIN.dp,
                        end = Constants.ITEM_PANEL_MARGIN.dp,
                        top = animatedCardTopDp
                    ),
                horizontalAlignment = androidx.compose.ui.BiasAlignment.Horizontal(animatedAlignmentBias)
            ) {
                // 白色卡片（宽度自适应内容，最大不超过 grid 宽度）
                val maxCardWidthDp = with(density) { (cellSizePx * 3).toDp() }
                AppColumn(
                    modifier = Modifier
                        .widthIn(max = maxCardWidthDp)
                        .onSizeChanged { cardHeightPx = it.height.toFloat() }
                        .alpha(if (cardHeightPx == 0f) 0f else 1f)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(
                            colorResource(R.color.color_white),
                            RoundedCornerShape(12.dp)
                        )
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        .padding(8.dp)
                        .alpha(animatedContentOpacity),
                    fillMaxWidth = false
                ) {
                    AppText(
                        text = displayData.name ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.color_black)
                    )
                    if (!displayData.desc.isNullOrEmpty()) {
                        VerticalSpace(height = 8.dp)
                        AppText(
                            text = displayData.desc ?: "",
                            fontSize = 15.sp,
                            color = colorResource(R.color.color_black)
                        )
                    }
                }

                // 箭头（三角形朝下，指向 item 中心）
                Canvas(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth()
                        .alpha(animatedContentOpacity)
                ) {
                    val arrowWidth = 12.dp.toPx()
                    val arrowHeight = 8.dp.toPx()
                    val centerX = animatedArrowXPx.coerceIn(arrowWidth / 2, size.width - arrowWidth / 2)
                    val path = Path().apply {
                        moveTo(centerX - arrowWidth / 2, 0f)
                        lineTo(centerX, arrowHeight)
                        lineTo(centerX + arrowWidth / 2, 0f)
                        close()
                    }
                    drawPath(path, arrowColor)
                }
            }
        }
    }
}
