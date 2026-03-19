package com.yd.weather.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.utils.formatDateStr

@Composable
fun WeatherForecase40Chart(
    forecast40: List<WeatherDetailData>,
    currentSelectedItem: WeatherDetailData?,
    onItemSelected: (WeatherDetailData) -> Unit
) {
    if (forecast40.isEmpty()) return

    val maxHigh = remember(forecast40) { forecast40.maxOf { it.high } }
    val minHigh = remember(forecast40) { forecast40.minOf { it.high } }
    val textMeasurer = rememberTextMeasurer()

    val white = colorResource(R.color.color_white)
    val blue = colorResource(R.color.color_0da8ff)

    val hapticFeedback = LocalHapticFeedback.current
    var currentItem by remember(currentSelectedItem) { mutableStateOf(currentSelectedItem) }

    val selectItem: (WeatherDetailData) -> Unit = { item ->
        if (item.date != currentItem?.date) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentItem = item
            onItemSelected(item)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tooltip bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 32.dp)
        ) {
            // Tooltip is drawn inside Canvas below for precise positioning
        }

        // Chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Date labels at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.BottomStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateIndices = listOf(0, 9, 19, 29, 39).filter { it < forecast40.size }
                dateIndices.forEach { idx ->
                    val dateStr = formatDateStr(forecast40[idx].date, "MM/dd") ?: ""
                    AppText(
                        text = dateStr,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        color = white,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Canvas for chart drawing
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(forecast40) {
                        detectTapGestures { offset ->
                            val item = hitTest(forecast40, offset.x, size.width.toFloat())
                            if (item != null) selectItem(item)
                        }
                    }
                    .pointerInput(forecast40) {
                        detectHorizontalDragGestures { change, _ ->
                            val item = hitTest(forecast40, change.position.x, size.width.toFloat())
                            if (item != null) selectItem(item)
                        }
                    }
            ) {
                val hGap = 32.dp.toPx()
                val bGap = 28.dp.toPx()
                val chartRight = size.width - hGap
                val chartTop = 0f
                val chartBottom = size.height - bGap

                val length = forecast40.size
                val gaps = 2.5.dp.toPx()
                val radius =
                    (chartRight - hGap - (length - 1) * gaps) / length / 2f

                val lineTop = chartTop + 24.dp.toPx()
                val lineBottom = chartBottom - 48.dp.toPx()

                // Draw horizontal dash lines
                drawDashLine(hGap, chartRight, lineTop, radius, white)
                drawDashLine(
                    hGap,
                    chartRight,
                    lineTop + (lineBottom - lineTop) / 2f,
                    radius,
                    white
                )
                drawDashLine(hGap, chartRight, lineBottom, radius, white)

                // Draw Y-axis labels
                val labelStyle = TextStyle(
                    fontSize = 11.sp,
                    color = white,
                    textAlign = TextAlign.Center
                )
                drawText(
                    textMeasurer,
                    "${maxHigh}°",
                    Offset(6.dp.toPx(), lineTop - 6.dp.toPx()),
                    style = labelStyle
                )
                drawText(
                    textMeasurer,
                    "${minHigh}°",
                    Offset(6.dp.toPx(), lineTop + (lineBottom - lineTop) - 6.dp.toPx()),
                    style = labelStyle
                )
                drawText(
                    textMeasurer,
                    "水",
                    Offset(8.dp.toPx(), chartBottom - 10.dp.toPx()),
                    style = labelStyle
                )

                // Draw vertical lines + circles + temperature line
                val tempPath = Path()
                var selectedPoint: Offset? = null

                forecast40.forEachIndexed { index, data ->
                    val isSelected = data.date == currentItem?.date
                    val isRain = isRainType(data)

                    val cx =
                        hGap + (2 * radius + gaps) * index + radius
                    val circleCenterY = chartBottom - radius

                    // Vertical line
                    drawLine(
                        color = if (isSelected) blue else white.copy(alpha = 0.4f),
                        start = Offset(cx, circleCenterY - radius),
                        end = Offset(cx, lineTop),
                        strokeWidth = if (isSelected) 2.dp.toPx() else 0.5.dp.toPx()
                    )

                    // Bottom circle
                    drawCircle(
                        color = if (isRain) blue else white,
                        radius = radius,
                        center = Offset(cx, circleCenterY)
                    )

                    // Temperature line point
                    val tempRange = (maxHigh - minHigh).coerceAtLeast(1)
                    val percent = (data.high - minHigh).toFloat() / tempRange
                    val pointY = lineBottom - (lineBottom - lineTop) * percent

                    if (index == 0) {
                        tempPath.moveTo(cx, pointY)
                    } else {
                        tempPath.lineTo(cx, pointY)
                    }

                    if (isSelected) {
                        selectedPoint = Offset(cx, pointY)
                    }
                }

                // Draw temperature line
                drawPath(
                    path = tempPath,
                    color = blue,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw selected point highlight
                selectedPoint?.let { pt ->
                    // Outer white circle with shadow
                    drawCircle(
                        color = white,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    // Inner blue circle
                    drawCircle(
                        color = blue,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }

                // Draw tooltip bubble
                currentItem?.let { item ->
                    val idx = forecast40.indexOf(item)
                    if (idx >= 0) {
                        val cx =
                            hGap + (2 * radius + gaps) * idx + radius
                        val desc =
                            "${formatDateStr(item.date, "MM月dd日") ?: ""} ${item.day?.wthr ?: ""} ${item.low}°~${item.high}°"
                        val measured = textMeasurer.measure(
                            desc,
                            TextStyle(fontSize = 12.sp, color = Color.Black)
                        )
                        val bubbleWidth = measured.size.width + 16.dp.toPx()
                        val bubbleHeight = 24.dp.toPx()

                        // Position bubble centered on cx, clamped to chart bounds
                        val fixedCx = cx - hGap - bubbleWidth / 2f
                        val bubbleX =
                            (hGap + fixedCx.coerceIn(0f, chartRight - hGap - bubbleWidth))
                        val bubbleY = -14.dp.toPx()

                        drawRoundRect(
                            color = white,
                            topLeft = Offset(bubbleX, bubbleY),
                            size = Size(
                                bubbleWidth,
                                bubbleHeight
                            ),
                            cornerRadius = CornerRadius(
                                100f,
                                100f
                            )
                        )
                        drawText(
                            textMeasurer,
                            desc,
                            Offset(
                                bubbleX + 8.dp.toPx(),
                                bubbleY + (bubbleHeight - measured.size.height) / 2f
                            ),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDashLine(
    left: Float,
    right: Float,
    y: Float,
    radius: Float,
    color: Color
) {
    val dashWidth = 2.dp.toPx()
    val spaceWidth = 8.dp.toPx()
    drawLine(
        color = color.copy(alpha = 0.8f),
        start = Offset(left + radius, y),
        end = Offset(right - radius, y),
        strokeWidth = 0.8.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashWidth, spaceWidth),
            0f
        )
    )
}

private fun hitTest(
    forecast40: List<WeatherDetailData>,
    touchX: Float,
    canvasWidth: Float
): WeatherDetailData? {
    if (forecast40.isEmpty()) return null
    val hGap = 32f // approximate dp, will be close enough for hit testing
    val chartLeft = hGap
    val chartRight = canvasWidth - hGap
    val chartWidth = chartRight - chartLeft
    val length = forecast40.size
    var index = ((touchX - chartLeft) / (chartWidth / length)).toInt()
    index = index.coerceIn(0, length - 1)
    return forecast40[index]
}

private fun isRainType(data: WeatherDetailData): Boolean {
    val type = data.day?.thirdType ?: return false
    return type == "LIGHT_RAIN" || type == "MODERATE_RAIN" ||
            type == "HEAVY_RAIN" || type == "STORM_RAIN"
}
