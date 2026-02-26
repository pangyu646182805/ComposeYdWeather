package com.yd.weather.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * iOS 风格越界回弹效果
 *
 * 在 LazyColumn / LazyVerticalGrid 等滚动容器上使用，
 * 滚动到边界后继续拖拽内容会跟随偏移（橡皮筋效果），
 * 松手后弹簧动画回归原位。
 *
 * @param maxOffsetPx 最大允许越界偏移量（px），默认 300f
 */
fun Modifier.iosBounceScroll(
    maxOffsetPx: Float = 300f,
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val connection = remember {
        object : NestedScrollConnection {

            // 子组件消费后剩余的滚动量 → 越界偏移（橡皮筋）
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    scope.launch {
                        // 越界越深，阻力越大
                        val resistance = (1f - abs(offsetY.value) / maxOffsetPx).coerceIn(0f, 1f)
                        val newOffset = (offsetY.value + available.y * resistance * 0.4f)
                            .coerceIn(-maxOffsetPx, maxOffsetPx)
                        offsetY.snapTo(newOffset)
                    }
                    return available
                }
                return Offset.Zero
            }

            // 手指抬起时（含无速度抬起）→ 弹簧回位
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value != 0f) {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }
                return Velocity.Zero  // 不消耗速度，让列表正常 fling
            }
        }
    }

    this
        .nestedScroll(connection)
        .graphicsLayer { translationY = offsetY.value }
}
