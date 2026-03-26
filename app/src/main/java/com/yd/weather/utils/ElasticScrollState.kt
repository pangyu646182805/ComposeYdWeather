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
 * 列表到边界后继续拖拽，内容跟随手指带橡皮筋阻尼；松手后 spring 回弹。
 * 支持垂直和水平方向，支持超过阈值时触发回调（如切换城市）。
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
     * 用于 graphicsLayer { translationY/translationX = overscrollOffset }
     */
    val overscrollOffset: Float get() = _offset.value

    private val isVertical get() = orientation == Orientation.Vertical

    // 从 Offset/Velocity 中取对应轴的值
    private fun Offset.main() = if (isVertical) y else x
    private fun Velocity.main() = if (isVertical) y else x
    private fun mainOffset(value: Float) = if (isVertical) Offset(0f, value) else Offset(value, 0f)
    private fun mainVelocity(value: Float) = if (isVertical) Velocity(0f, value) else Velocity(value, 0f)

    // ---- 配置 ----

    /** 阻尼系数，越小拉动越吃力，iOS 约 0.3-0.5 */
    var resistance = 0.35f

    /** 回弹弹簧刚度 */
    var springStiffness = Spring.StiffnessMediumLow

    /** 回弹弹簧阻尼比 */
    var springDampingRatio = Spring.DampingRatioNoBouncy

    // ---- 正向/反向阈值回调 ----

    /**
     * 反向超过阈值松手时触发（垂直=上拉，水平=左拉）
     */
    var onOverPullEnd: (() -> Unit)? = null

    /**
     * 正向超过阈值松手时触发（垂直=下拉，水平=右拉）
     */
    var onOverPullStart: (() -> Unit)? = null

    /** 反向触发阈值（正值，offset 绝对值超过此值触发） */
    var pullEndThreshold = 0f

    /** 正向触发阈值 */
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

    private fun snap(delta: Float) {
        if (delta == 0f) return
        coroutineScope.launch {
            mutatorMutex.mutate(MutatePriority.UserInput) {
                _offset.snapTo(_offset.value + delta)
            }
        }
    }

    private suspend fun springBack() {
        mutatorMutex.mutate {
            _offset.animateTo(
                0f,
                spring(dampingRatio = springDampingRatio, stiffness = springStiffness)
            )
        }
    }

    /** iOS 风格阻尼：越远越难拉 */
    private fun dampedDelta(delta: Float): Float {
        val maxDrag = 600f
        val progress = (abs(_offset.value) / maxDrag).coerceIn(0f, 0.9f)
        return delta * resistance * (1f - progress)
    }

    /** 根据剩余速度计算 fling 到边界后的弹性冲击距离 */
    private fun flingOvershoot(velocity: Float): Float {
        val maxOvershoot = 300f
        val raw = velocity * 0.08f
        return raw.coerceIn(-maxOvershoot, maxOvershoot)
    }

    val connection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val mainAvailable = available.main()
            val offset = _offset.value
            return when {
                // offset > 0 且反向滑动 → 收回
                offset > 0 && mainAvailable < 0 -> {
                    val consume = mainAvailable.coerceAtLeast(-offset / resistance)
                    snap(dampedDelta(consume))
                    mainOffset(consume)
                }
                // offset < 0 且正向滑动 → 收回
                offset < 0 && mainAvailable > 0 -> {
                    val consume = mainAvailable.coerceAtMost(-offset / resistance)
                    snap(dampedDelta(consume))
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
            // fling 过程中到达边界：不累积，交给 onPostFling 处理弹性冲击
            if (source == NestedScrollSource.SideEffect) return Offset.Zero
            // 手指拖拽：带阻尼跟随
            val damped = dampedDelta(mainAvailable)
            snap(damped)
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val offset = _offset.value
            if (offset < 0 && pullEndThreshold > 0 && abs(offset) >= pullEndThreshold) {
                onOverPullEnd?.invoke()
            }
            if (offset > 0 && pullStartThreshold > 0 && offset >= pullStartThreshold) {
                onOverPullStart?.invoke()
            }
            if (offset != 0f) {
                springBack()
                return available
            }
            return Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val mainAvailable = available.main()
            if (mainAvailable != 0f) {
                val overshoot = flingOvershoot(mainAvailable)
                if (overshoot != 0f) {
                    mutatorMutex.mutate(MutatePriority.UserInput) {
                        _offset.snapTo(overshoot)
                    }
                    springBack()
                }
                return available
            }
            if (_offset.value != 0f) {
                springBack()
                return available
            }
            return Velocity.Zero
        }
    }
}
