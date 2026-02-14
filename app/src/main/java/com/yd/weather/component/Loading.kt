package com.yd.weather.component

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yd.weather.res.YdWeatherAppTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 多点加载动画 - 3个圆点依次高亮显示
 *
 * @param color 圆点颜色，默认使用outline颜色
 * @param animationSpec 动画规格配置，默认1000ms
 */
@Composable
fun WeLoadingMP(
    color: Color = MaterialTheme.colorScheme.outline,
    animationSpec: DurationBasedAnimationSpec<Float> = tween(durationMillis = 1000)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val currentIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = animationSpec,
            repeatMode = RepeatMode.Restart
        ),
        label = "WeLoadingMPAnimation"
    )

    Canvas(modifier = Modifier.size(width = 44.dp, height = 20.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = (size.width - 2 * dotRadius) / 2

        repeat(3) { index ->
            val isActive = index == currentIndex.roundToInt()
            val dotColor = color.copy(alpha = if (isActive) 0.8f else 0.4f)
            val center = Offset(
                x = dotRadius + spacing * index,
                y = size.height / 2
            )

            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = center
            )
        }
    }
}

/**
 * 小米风格移动端加载动画 - 圆形轨道上的圆点旋转
 *
 * @param borderColor 圆形轨道边框颜色，默认使用onSurface颜色
 * @param dotColor 旋转圆点的颜色，默认与边框颜色相同
 * @param animationSpec 动画规格配置，默认1200ms线性动画
 */
@Composable
fun MiLoadingMobile(
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    dotColor: Color = borderColor,
    animationSpec: DurationBasedAnimationSpec<Float> = tween(
        durationMillis = 1200,
        easing = LinearEasing
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = animationSpec,
            repeatMode = RepeatMode.Restart
        ),
        label = "MiLoadingMobileAnimation"
    )

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .border(2.dp, borderColor, CircleShape)
    ) {
        val circleRadius = size.minDimension / 2 - 8.dp.toPx()
        val dotRadius = 3.dp.toPx()
        val center = size.center
        val dotX = cos(Math.toRadians(angle.value.toDouble())) * circleRadius + center.x
        val dotY = sin(Math.toRadians(angle.value.toDouble())) * circleRadius + center.y

        drawCircle(dotColor, radius = dotRadius, center = Offset(dotX.toFloat(), dotY.toFloat()))
    }
}

/**
 * WeLoadingMP组件预览
 */
@Preview(showBackground = true)
@Composable
fun WeLoadingMPPreview() {
    YdWeatherAppTheme() {
        WeLoadingMP()
    }
}

/**
 * 小米风格移动端加载动画
 */
@Preview(showBackground = true)
@Composable
fun MiLoadingMobilePreview() {
    YdWeatherAppTheme {
        MiLoadingMobile()
    }
}
