package com.yd.weather.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.model.WeatherDetailData
import com.yd.weather.utils.Commons
import com.yd.weather.utils.isToday

@Composable
fun WeatherDailyTempPanel(
    panelWidth: Dp,
    panelHeight: Dp,
    preData: WeatherDetailData? = null,
    data: WeatherDetailData? = null,
    nextData: WeatherDetailData? = null,
    maxTemp: Int = 0,
    minTemp: Int = 0
) {
    if (data == null || maxTemp == minTemp) return

    val density = LocalDensity.current
    val topAndBottomGaps = with(density) { 32.dp.toPx() }
    val widthPx = with(density) { panelWidth.toPx() }
    val heightPx = with(density) { panelHeight.toPx() }
    val textSizePx = with(density) { 13.sp.toPx() }
    val lineColor = Color.White
    val dimAlpha = 0.3f

    Canvas(
        modifier = Modifier
            .width(panelWidth)
            .height(panelHeight)
    ) {
        fun calTempYAxis(temp: Int): Float {
            val diff = maxTemp - temp
            val diffTemp = maxTemp - minTemp
            val percent = diff.toFloat() / diffTemp
            return (heightPx - 2 * topAndBottomGaps) * percent + topAndBottomGaps
        }

        fun drawTempText(text: String, color: Color, tempYAxis: Float, isHigh: Boolean) {
            val centerX = widthPx / 2
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.argb(
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                    this.textSize = textSizePx
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.isAntiAlias = true
                }
                val y = if (isHigh) tempYAxis - 4 * density.density else tempYAxis + 4 * density.density + textSizePx
                drawText(text, centerX, y, paint)
            }
        }

        // Draw start area (first item: no preData)
        fun drawStartArea(isHigh: Boolean) {
            if (preData == null && nextData != null) {
                val centerX = widthPx / 2
                val tempYAxis = calTempYAxis(if (isHigh) data.high else data.low)
                val nextTempYAxis = calTempYAxis(if (isHigh) nextData.high else nextData.low)

                val path = Path().apply {
                    moveTo(centerX, tempYAxis)
                    quadraticTo(
                        centerX + widthPx / 4,
                        (nextTempYAxis - tempYAxis) / 4 + tempYAxis,
                        widthPx,
                        (nextTempYAxis - tempYAxis) / 2 + tempYAxis
                    )
                }
                drawPath(path, lineColor.copy(alpha = dimAlpha), style = Stroke(width = 1 * density.density))

                drawCircle(
                    color = lineColor.copy(alpha = dimAlpha),
                    radius = 2.5f * density.density,
                    center = Offset(centerX, tempYAxis)
                )

                drawTempText(
                    Commons.getTemp(if (isHigh) data.high else data.low),
                    lineColor.copy(alpha = dimAlpha),
                    tempYAxis,
                    isHigh
                )
            }
        }

        // Draw center line helper
        fun DrawScope.drawCenterLine(
            moveToY: Float,
            p1: Offset,
            p2: Offset,
            tempYAxis: Float,
            nextTempYAxis: Float,
            color: Color
        ) {
            val path = Path().apply {
                moveTo(0f, moveToY)
                cubicTo(
                    p1.x, p1.y,
                    p2.x, p2.y,
                    widthPx, (nextTempYAxis - tempYAxis) / 2 + tempYAxis
                )
            }
            drawPath(path, color, style = Stroke(width = 1 * density.density))
        }

        // Draw center area (middle items: has both preData and nextData)
        fun drawCenterArea(isHigh: Boolean) {
            if (preData != null && nextData != null) {
                val centerX = widthPx / 2
                val dateIsToday = isToday(data.date)
                val dateIsBefore = Commons.isBefore(data.date)
                val tempYAxis = calTempYAxis(if (isHigh) data.high else data.low)
                val preTempYAxis = calTempYAxis(if (isHigh) preData.high else preData.low)
                val nextTempYAxis = calTempYAxis(if (isHigh) nextData.high else nextData.low)
                val moveToY = (preTempYAxis - tempYAxis) / 2 + tempYAxis
                val p1 = Offset(widthPx / 4, (moveToY - tempYAxis) / 2 + tempYAxis)
                val p2 = Offset(widthPx - widthPx / 4, (nextTempYAxis - tempYAxis) / 4 + tempYAxis)

                if (dateIsToday || dateIsBefore) {
                    // Right half: full opacity
                    drawContext.canvas.save()
                    drawContext.canvas.clipRect(
                        left = centerX,
                        top = 0f,
                        right = widthPx,
                        bottom = heightPx
                    )
                    drawCenterLine(moveToY, p1, p2, tempYAxis, nextTempYAxis, lineColor)
                    drawContext.canvas.restore()

                    // Left half: dim
                    drawContext.canvas.save()
                    drawContext.canvas.clipRect(
                        left = 0f,
                        top = 0f,
                        right = centerX,
                        bottom = heightPx
                    )
                    drawCenterLine(moveToY, p1, p2, tempYAxis, nextTempYAxis, lineColor.copy(alpha = dimAlpha))
                    drawContext.canvas.restore()
                } else {
                    drawCenterLine(moveToY, p1, p2, tempYAxis, nextTempYAxis, lineColor)
                }

                val dotY = (p2.y - p1.y) / 2 + p1.y
                drawCircle(
                    color = lineColor.copy(alpha = if (dateIsBefore) dimAlpha else 1f),
                    radius = 2.5f * density.density,
                    center = Offset(centerX, dotY)
                )

                drawTempText(
                    Commons.getTemp(if (isHigh) data.high else data.low),
                    lineColor.copy(alpha = if (dateIsBefore) dimAlpha else 1f),
                    dotY,
                    isHigh
                )
            }
        }

        // Draw end area (last item: no nextData)
        fun drawEndArea(isHigh: Boolean) {
            if (preData != null && nextData == null) {
                val centerX = widthPx / 2
                val tempYAxis = calTempYAxis(if (isHigh) data.high else data.low)
                val preTempYAxis = calTempYAxis(if (isHigh) preData.high else preData.low)

                val path = Path().apply {
                    moveTo(centerX, tempYAxis)
                    quadraticTo(
                        centerX - widthPx / 4,
                        (preTempYAxis - tempYAxis) / 4 + tempYAxis,
                        0f,
                        (preTempYAxis - tempYAxis) / 2 + tempYAxis
                    )
                }
                drawPath(path, lineColor, style = Stroke(width = 1 * density.density))

                drawCircle(
                    color = lineColor,
                    radius = 2.5f * density.density,
                    center = Offset(centerX, tempYAxis)
                )

                drawTempText(
                    Commons.getTemp(if (isHigh) data.high else data.low),
                    lineColor,
                    tempYAxis,
                    isHigh
                )
            }
        }

        drawStartArea(true)
        drawStartArea(false)
        drawCenterArea(true)
        drawCenterArea(false)
        drawEndArea(true)
        drawEndArea(false)
    }
}
