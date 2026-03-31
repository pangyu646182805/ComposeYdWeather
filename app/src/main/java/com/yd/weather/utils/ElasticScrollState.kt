package com.yd.weather.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * iOS 风格弹性滚动状态
 *
 * 列表到边界后继续拖拽，内容跟随手指带橡皮筋阻尼；松手后快速回弹。
 * 模拟 iOS UIScrollView 的 bounces 行为。
 *
 * 用法（垂直）:
 * ```
 * val elastic = rememberElasticScrollState()
 * LazyColumn(
 *     modifier = Modifier
 *         .nestedScroll(elastic.connection)
 *         .graphicsLayer { translationY = elastic.overscrollOffset }
 * )
 * ```
 *
 * 用法（水平）:
 * ```
 * val elastic = rememberElasticScrollState(orientation = Orientation.Horizontal)
 * LazyRow(
 *     modifier = Modifier
 *         .nestedScroll(elastic.connection)
 *         .graphicsLayer { translationX = elastic.overscrollOffset }
 * )
 * ```
 */
@Composable
fun rememberElasticScrollState(
    orientation: Orientation = Orientation.Vertical,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): ElasticScrollState {
    return remember { ElasticScrollState(orientation, coroutineScope) }
}

@Stable
class ElasticScrollState(
    val orientation: Orientation = Orientation.Vertical,
    private val coroutineScope: CoroutineScope
) {

    private val _offset = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    /**
     * 当前过度滚动偏移量
     * 垂直：正=下拉，负=上拉；水平：正=右拉，负=左拉
     */
    val overscrollOffset: Float get() = _offset.value

    private val isVertical get() = orientation == Orientation.Vertical

    private fun Offset.main() = if (isVertical) y else x
    private fun Velocity.main() = if (isVertical) y else x
    private fun mainOffset(value: Float) = if (isVertical) Offset(0f, value) else Offset(value, 0f)

    // ---- 配置 ----

    /**
     * iOS 橡皮筋阻尼系数，iOS 默认约 0.55
     * 实际公式: dampedOffset = delta * resistance * (1 - |currentOffset| / maxDrag)
     */
    var resistance = 0.55f

    /** 最大拖拽距离（px），越大越容易拉远 */
    private val maxDrag = 400f

    /** fling 到边缘的最大冲击距离（px） */
    private val maxFlingOvershoot = 80f

    // ---- 正向/反向阈值回调 ----

    var onOverPullEnd: (() -> Unit)? = null
    var onOverPullStart: (() -> Unit)? = null
    var pullEndThreshold = 0f
    var pullStartThreshold = 0f

    // 兼容旧 API
    var onOverPullUp: (() -> Unit)?
        get() = onOverPullEnd
        set(value) { onOverPullEnd = value }
    var onOverPullDown: (() -> Unit)?
        get() = onOverPullStart
        set(value) { onOverPullStart = value }
    var pullUpThreshold: Float
        get() = pullEndThreshold
        set(value) { pullEndThreshold = value }
    var pullDownThreshold: Float
        get() = pullStartThreshold
        set(value) { pullStartThreshold = value }

    // ---- 内部方法 ----

    private fun consumed(delta: Float) {
        if (delta == 0f) return
        coroutineScope.launch {
            mutatorMutex.mutate(MutatePriority.UserInput) {
                _offset.snapTo(_offset.value + delta)
            }
        }
    }

    /**
     * iOS 临界阻尼回弹：无振荡，快速收回
     */
    private suspend fun springBack() {
        mutatorMutex.mutate {
            _offset.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 500f
                )
            )
        }
    }

    /**
     * iOS 橡皮筋阻尼
     * 越远越难拉，接近 maxDrag 时几乎不动
     */
    private fun rubberbandDelta(delta: Float): Float {
        val absOffset = abs(_offset.value)
        val progress = (absOffset / maxDrag).coerceIn(0f, 0.95f)
        return delta * resistance * (1f - progress)
    }

    val connection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val mainAvailable = available.main()
            val offset = _offset.value
            return when {
                // 有正向弹性偏移且反向滑动 → 收回
                offset > 0 && mainAvailable < 0 -> {
                    val consume = mainAvailable.coerceAtLeast(-offset / resistance)
                    consumed(rubberbandDelta(consume))
                    mainOffset(consume)
                }
                // 有负向弹性偏移且正向滑动 → 收回
                offset < 0 && mainAvailable > 0 -> {
                    val consume = mainAvailable.coerceAtMost(-offset / resistance)
                    consumed(rubberbandDelta(consume))
                    mainOffset(consume)
                }
                else -> Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val mainAvailable = available.main()
            if (mainAvailable == 0f) return Offset.Zero
            // fling 到边缘不累积，交给 onPostFling 处理
            if (source == NestedScrollSource.SideEffect) return Offset.Zero
            // 手指拖拽：橡皮筋阻尼
            consumed(rubberbandDelta(mainAvailable))
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val offset = _offset.value
            // 检查阈值回调
            if (offset < 0 && pullEndThreshold > 0 && abs(offset) >= pullEndThreshold) {
                onOverPullEnd?.invoke()
            }
            if (offset > 0 && pullStartThreshold > 0 && offset >= pullStartThreshold) {
                onOverPullStart?.invoke()
            }
            // 松手时有弹性偏移 → 回弹
            if (offset != 0f) {
                springBack()
                return available
            }
            return Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val mainVelocity = available.main()
            if (mainVelocity != 0f && _offset.value == 0f) {
                // fling 剩余速度到边缘：计算小幅 overshoot
                // iOS 风格：速度越大冲击越大，但有上限
                val overshoot = (mainVelocity * 0.02f)
                    .coerceIn(-maxFlingOvershoot, maxFlingOvershoot)
                if (abs(overshoot) > 2f) {
                    mutatorMutex.mutate {
                        // snap 到冲击位置，然后临界阻尼回弹
                        _offset.snapTo(overshoot)
                        _offset.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = 500f
                            )
                        )
                    }
                    return available
                }
            }
            if (_offset.value != 0f) {
                springBack()
                return available
            }
            return Velocity.Zero
        }
    }
}
