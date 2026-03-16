package com.yd.weather.widget

import android.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yd.weather.R
import java.util.Calendar

@Composable
fun WeatherObserveSunriseSunsetChart(
    panelWidth: Dp,
    panelHeight: Dp,
    sunrise: String = "",
    sunset: String = ""
) {
    val whiteColor = colorResource(R.color.color_white)

    // 计算当前时间在日出日落中的百分比位置
    val percent = calcSunPercent(sunrise, sunset)

    Canvas(
        modifier = Modifier
            .size(panelWidth, panelHeight)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        val strokeWidth = 4.dp.toPx()
        val right = size.width - strokeWidth
        val bottom = size.height - strokeWidth
        val width = right - strokeWidth
        val height = bottom - strokeWidth
        val centerX = size.width / 2
        val lineY = strokeWidth + height * 0.8f

        // 水平分界线
        drawLine(
            color = whiteColor.copy(alpha = 0.4f),
            start = Offset(0f, lineY),
            end = Offset(right, lineY),
            strokeWidth = 0.5.dp.toPx()
        )

        // 贝塞尔曲线控制点
        val p1 = Offset(strokeWidth + width / 4 + width / 8, bottom)
        val p2 = Offset(strokeWidth + width / 4 - width / 8, strokeWidth)
        val p3 = Offset(right - width / 4 + width / 8, strokeWidth)
        val p4 = Offset(right - width / 4 - width / 8, bottom)

        // 构建完整曲线路径
        val fullPath = Path().apply {
            moveTo(strokeWidth, bottom)
            cubicTo(p1.x, p1.y, p2.x, p2.y, centerX, strokeWidth)
            cubicTo(p3.x, p3.y, p4.x, p4.y, right, bottom)
        }

        // 水平线以上部分：实色白色
        clipRect(0f, 0f, size.width, lineY) {
            drawPath(
                path = fullPath,
                color = whiteColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 水平线以下部分：半透明白色
        clipRect(0f, lineY, size.width, size.height) {
            drawPath(
                path = fullPath,
                color = whiteColor.copy(alpha = 0.4f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 通过 PathMeasure 计算指示点位置
        if (percent > 0f) {
            val androidPath = fullPath.asAndroidPath()
            val pathMeasure = PathMeasure(androidPath, false)
            val pos = FloatArray(2)
            val tan = FloatArray(2)
            pathMeasure.getPosTan(pathMeasure.length * percent, pos, tan)

            val dotCenter = Offset(pos[0], pos[1])
            val dotAlpha = if (percent < 0.2f || percent > 0.8f) 0.4f else 1f

            // 擦除圆（留出间隙）
            drawCircle(
                color = Color.Transparent,
                radius = 7.dp.toPx(),
                center = dotCenter,
                blendMode = BlendMode.Clear
            )

            // 指示圆点
            drawCircle(
                color = whiteColor.copy(alpha = dotAlpha),
                radius = 4.dp.toPx(),
                center = dotCenter,
                blendMode = BlendMode.SrcOver
            )
        }
    }
}

/**
 * 计算当前时间在一天中相对日出日落的百分比位置
 * 日出前: 0~0.2, 日出到日落: 0.2~0.8, 日落后: 0.8~1.0
 */
private fun calcSunPercent(sunrise: String, sunset: String): Float {
    if (sunrise.isEmpty() || sunset.isEmpty()) return 0f
    val sunriseMinutes = parseMinutes(sunrise) ?: return 0f
    val sunsetMinutes = parseMinutes(sunset) ?: return 0f

    val now = Calendar.getInstance()
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    val percent = when {
        currentMinutes < sunriseMinutes -> {
            // 日出前：0点到日出映射到 0~0.2
            currentMinutes.toFloat() / sunriseMinutes * 0.2f
        }
        currentMinutes < sunsetMinutes -> {
            // 日出到日落：映射到 0.2~0.8
            0.2f + (currentMinutes - sunriseMinutes).toFloat() / (sunsetMinutes - sunriseMinutes) * 0.6f
        }
        else -> {
            // 日落后：日落到24点映射到 0.8~1.0
            0.8f + (currentMinutes - sunsetMinutes).toFloat() / (1440 - sunsetMinutes) * 0.2f
        }
    }
    return percent.coerceIn(0f, 1f)
}

private fun parseMinutes(time: String): Int? {
    val parts = time.split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}
