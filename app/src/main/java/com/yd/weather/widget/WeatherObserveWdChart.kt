package com.yd.weather.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.res.CommonIcon

@Composable
fun WeatherObserveWdChart(
    panelWidth: Dp,
    panelHeight: Dp,
    wd: String = ""
) {
    val accentColor = colorResource(R.color.color_0da8ff)
    val whiteColor = colorResource(R.color.color_white)

    // 风向对应的旋转角度（以东风=0°为基准，顺时针）
    val wdAngle = when (wd) {
        "东风" -> 0f
        "南风" -> 90f
        "西风" -> 180f
        "北风" -> 270f
        "东南风" -> 45f
        "西南风" -> 135f
        "西北风" -> 225f
        "东北风" -> 315f
        else -> 0f
    }

    Box(
        modifier = Modifier.size(panelWidth, panelHeight),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(panelWidth, panelHeight)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val tickLength = 6.dp.toPx()
            val outerRadius = centerX

            // 绘制 72 个刻度
            for (i in 0 until 72) {
                val angle = i * (360f / 72)
                val isMain = i == 0 || i == 18 || i == 36 || i == 54
                val alpha = if (isMain) 1f else 0.4f

                rotate(angle, pivot = Offset(centerX, centerY)) {
                    drawLine(
                        color = whiteColor.copy(alpha = alpha),
                        start = Offset(centerX + outerRadius - tickLength, centerY),
                        end = Offset(centerX + outerRadius, centerY),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                }
            }

            // 绘制风向指针
            if (wd.isNotEmpty()) {
                rotate(wdAngle, pivot = Offset(centerX, centerY)) {
                    val pointerEnd = outerRadius - tickLength
                    val arrowTip = -outerRadius + tickLength
                    val barHalf = 0.8.dp.toPx()
                    val arrowInset = arrowTip + 6.dp.toPx()
                    val arrowWing = 3.dp.toPx()

                    // 指针箭头形状
                    val arrowPath = Path().apply {
                        // 右侧杆
                        moveTo(centerX + pointerEnd, centerY - barHalf)
                        lineTo(centerX + arrowInset, centerY - barHalf)
                        // 箭头上翼
                        lineTo(centerX + arrowInset, centerY - barHalf - arrowWing)
                        // 箭尖
                        lineTo(centerX + arrowTip, centerY)
                        // 箭头下翼
                        lineTo(centerX + arrowInset, centerY + barHalf + arrowWing)
                        lineTo(centerX + arrowInset, centerY + barHalf)
                        // 左侧杆
                        lineTo(centerX + pointerEnd, centerY + barHalf)
                        close()
                    }
                    drawPath(path = arrowPath, color = accentColor)

                    // 指针尾部圆点（蓝底白心）
                    drawCircle(
                        color = accentColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX + pointerEnd, centerY)
                    )
                    drawCircle(
                        color = whiteColor,
                        radius = 2.dp.toPx(),
                        center = Offset(centerX + pointerEnd, centerY)
                    )
                }
            }
        }

        // 中心风向图标
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CommonIcon(
                resId = R.mipmap.ic_wd_icon,
                size = 12.dp,
                tint = whiteColor
            )
        }

        // 北
        AppText(
            text = "北",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            fontSize = 11.sp,
            color = whiteColor,
            fontWeight = FontWeight.Bold
        )
        // 南
        AppText(
            text = "南",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            fontSize = 11.sp,
            color = whiteColor,
            fontWeight = FontWeight.Bold
        )
        // 西
        AppText(
            text = "西",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
            fontSize = 11.sp,
            color = whiteColor,
            fontWeight = FontWeight.Bold
        )
        // 东
        AppText(
            text = "东",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            fontSize = 11.sp,
            color = whiteColor,
            fontWeight = FontWeight.Bold
        )
    }
}
