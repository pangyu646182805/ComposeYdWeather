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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText

@Composable
fun WeatherObserveUvChart(
    panelWidth: Dp,
    panelHeight: Dp,
    uvIndex: Int = 0,
    uvIndexMax: Int = 0
) {
    val percent = if (uvIndexMax > 0) {
        (uvIndex.toFloat() / uvIndexMax).coerceIn(0f, 1f)
    } else 0f

    val dotColor = when {
        percent > 0.8f -> Color(0xFFBB10DE)
        percent > 0.6f -> Color(0xFFFC0B23)
        percent > 0.4f -> Color(0xFFFE5B21)
        percent > 0.2f -> Color(0xFFFFDE00)
        else -> Color(0xFF37CA00)
    }

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

            // 构建弧形路径，起始角 -225°（即 135°），扫过 270°
            val arcPath = Path().apply {
                arcTo(
                    rect = arcRect,
                    startAngleDegrees = -225f,
                    sweepAngleDegrees = 270f,
                    forceMoveTo = false
                )
            }

            // SweepGradient（锥形渐变），与 Flutter 的 Gradient.sweep 对应
            val sweepShader = SweepGradientShader(
                center = arcRect.center,
                colors = listOf(
                    Color(0xFFBB10DE),
                    Color(0xFFBB10DE),
                    Color(0xFF37CA00),
                    Color(0xFF37CA00),
                    Color(0xFFFFDE00),
                    Color(0xFFFE5B21),
                    Color(0xFFFC0B23),
                    Color(0xFFBB10DE),
                ),
                colorStops = listOf(
                    0.125f,
                    0.25f,
                    0.375f,
                    0.45f,
                    0.625f,
                    0.75f,
                    0.925f,
                    1.0f,
                )
            )

            // 使用渐变画弧
            drawPath(
                path = arcPath,
                brush = ShaderBrush(sweepShader),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 通过 PathMeasure 计算指示点在弧上的位置
            if (uvIndexMax > 0) {
                val androidPath = arcPath.asAndroidPath()
                val pathMeasure = PathMeasure(androidPath, false)
                val pos = FloatArray(2)
                val tan = FloatArray(2)
                pathMeasure.getPosTan(pathMeasure.length * percent, pos, tan)

                val dotCenter = Offset(pos[0], pos[1])

                // 先画透明圆擦除弧线（模拟 BlendMode.clear）
                drawCircle(
                    color = Color.Transparent,
                    radius = 7.dp.toPx(),
                    center = dotCenter,
                    blendMode = BlendMode.Clear
                )

                // 再画实色指示点
                drawCircle(
                    color = dotColor,
                    radius = 4.dp.toPx(),
                    center = dotCenter,
                    blendMode = BlendMode.SrcOver
                )
            }
        }

        // UV 数值
        AppText(
            text = uvIndex.toString(),
            fontSize = 18.sp,
            color = colorResource(R.color.color_white),
            fontWeight = FontWeight.Bold
        )

        // UV 标签
        AppText(
            text = "UV",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            fontSize = 11.sp,
            color = colorResource(R.color.color_white)
        )
    }
}
