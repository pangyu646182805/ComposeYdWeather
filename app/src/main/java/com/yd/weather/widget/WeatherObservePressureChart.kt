package com.yd.weather.widget

import android.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.res.CommonIcon

@Composable
fun WeatherObservePressureChart(
    panelWidth: Dp,
    panelHeight: Dp,
    pressure: String = ""
) {
    val whiteColor = colorResource(R.color.color_white)

    // 解析气压值并计算百分比
    val pressureValue = pressure.replace("hPa", "").trim().toIntOrNull() ?: 0
    val (min, max) = when {
        pressureValue < 100 -> 0 to 200
        pressureValue < 1000 -> {
            val m = pressureValue - pressureValue % 100
            m to (m + 300)
        }

        else -> {
            val m = pressureValue - pressureValue % 1000
            m to (m + 300)
        }
    }
    val percent = if (max > min) {
        ((pressureValue - min).toFloat() / (max - min)).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier.size(panelWidth, panelHeight),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(panelWidth, panelHeight)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val strokeWidth = 4.dp.toPx()
            val arcRect = Rect(
                left = strokeWidth,
                top = strokeWidth,
                right = size.width - strokeWidth,
                bottom = size.height - strokeWidth
            )

            // 270° 开口弧
            val arcPath = Path().apply {
                arcTo(
                    rect = arcRect,
                    startAngleDegrees = -225f,
                    sweepAngleDegrees = 270f,
                    forceMoveTo = false
                )
            }

            drawPath(
                path = arcPath,
                color = whiteColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 指针刻度线
            if (pressureValue > 0) {
                val androidPath = arcPath.asAndroidPath()
                val pathMeasure = PathMeasure(androidPath, false)
                val pos = FloatArray(2)
                val tan = FloatArray(2)
                pathMeasure.getPosTan(pathMeasure.length * percent, pos, tan)

                val dotCenter = Offset(pos[0], pos[1])

                // 擦除间隙
                drawCircle(
                    color = Color.Transparent,
                    radius = 7.dp.toPx(),
                    center = dotCenter,
                    blendMode = BlendMode.Clear
                )

                // 计算刻度线方向（沿切线法线方向）
                val angle = kotlin.math.atan2(tan[1].toDouble(), tan[0].toDouble())
                val nx = -kotlin.math.sin(angle).toFloat()
                val ny = kotlin.math.cos(angle).toFloat()
                val halfLen = 4.dp.toPx()

                // 刻度线
                drawLine(
                    color = whiteColor,
                    start = Offset(dotCenter.x - nx * halfLen, dotCenter.y - ny * halfLen),
                    end = Offset(dotCenter.x + nx * halfLen, dotCenter.y + ny * halfLen),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.SrcOver
                )
            }
        }

        // 箭头图标
        CommonIcon(
            resId = R.mipmap.ic_arrow_down,
            size = 20.dp,
            tint = whiteColor
        )

        // hPa 标签
        AppText(
            text = "hPa",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            fontSize = 11.sp,
            color = whiteColor
        )
    }
}
