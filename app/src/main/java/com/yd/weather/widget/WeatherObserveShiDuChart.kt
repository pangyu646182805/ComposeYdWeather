package com.yd.weather.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.res.CommonIcon

@Composable
fun WeatherObserveShiDuChart(
    panelWidth: Dp,
    panelHeight: Dp,
    shiDu: String = ""
) {
    val shiDuValue = shiDu.replace("%", "").toIntOrNull() ?: 0
    val shiDuPercent = (shiDuValue / 100f).coerceIn(0f, 1f)

    val shiDuDesc = when {
        shiDuValue < 40 -> "干燥"
        shiDuValue < 70 -> "舒适"
        else -> "潮湿"
    }

    val whiteColor = colorResource(R.color.color_white)

    Box(
        modifier = Modifier.size(panelWidth, panelHeight),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(panelWidth, panelHeight)) {
            val strokeWidth = 4.dp.toPx()
            val arcRect = Rect(
                left = strokeWidth,
                top = strokeWidth,
                right = size.width - strokeWidth,
                bottom = size.height - strokeWidth
            )

            // 底层弧：半透明白色，270° 开口弧
            val bgPath = Path().apply {
                arcTo(
                    rect = arcRect,
                    startAngleDegrees = -225f,
                    sweepAngleDegrees = 270f,
                    forceMoveTo = false
                )
            }
            drawPath(
                path = bgPath,
                color = whiteColor.copy(alpha = 0.4f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 前景弧：实色白色，按湿度百分比
            if (shiDuPercent > 0f) {
                val fgPath = Path().apply {
                    arcTo(
                        rect = arcRect,
                        startAngleDegrees = -225f,
                        sweepAngleDegrees = 270f * shiDuPercent,
                        forceMoveTo = false
                    )
                }
                drawPath(
                    path = fgPath,
                    color = whiteColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // 水滴图标
        CommonIcon(
            resId = R.mipmap.ic_water_icon,
            size = 16.dp,
            tint = colorResource(R.color.color_white)
        )

        // 湿度描述
        AppText(
            text = shiDuDesc,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            fontSize = 11.sp,
            color = colorResource(R.color.color_white)
        )
    }
}
