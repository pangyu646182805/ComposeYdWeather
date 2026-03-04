package com.yd.weather.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * SwipeRevealLayout 的状态持有者，持有偏移动画并对外暴露 [close] 方法。
 *
 * 通过 [rememberSwipeRevealState] 创建，并传入 [SwipeRevealLayout]。
 * 外部可随时调用 [close] 将 item 收起（带弹簧动画）。
 */
class SwipeRevealState {
    internal val offsetX = Animatable(0f)

    /** 当前是否处于展开（左滑）状态 */
    val isOpen: Boolean by derivedStateOf { offsetX.value < 0f }

    /** 收起 item，带弹簧动画（suspend，需在协程中调用） */
    suspend fun close() {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
}

@Composable
fun rememberSwipeRevealState() = remember { SwipeRevealState() }

/**
 * 左滑显示操作区、右滑回弹的滑动布局组件。
 *
 * @param state            状态对象，可通过 [rememberSwipeRevealState] 创建；
 *                         持有动画偏移，并对外暴露 [SwipeRevealState.close]
 * @param revealWidth      左滑最大距离，同时也是右侧操作区宽度
 * @param rightBounceWidth 右滑最大距离（松手后弹回原位）
 * @param enabled          是否允许拖拽（由父级控制，防止多 item 同时侧滑）
 * @param onDragStarted    拖拽开始回调
 * @param onDragStopped    拖拽结束回调
 * @param revealContent    左滑后露出的操作区内容（已对齐到右侧）
 * @param content          主体可拖拽内容
 */
@Composable
fun SwipeRevealLayout(
    modifier: Modifier = Modifier,
    state: SwipeRevealState = rememberSwipeRevealState(),
    revealWidth: Dp = 65.dp,
    rightBounceWidth: Dp = 65.dp,
    enabled: Boolean = true,
    onDragStarted: () -> Unit = {},
    onDragStopped: () -> Unit = {},
    revealContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val maxSwipePx = with(density) { revealWidth.toPx() }
    val rightBouncePx = with(density) { rightBounceWidth.toPx() }

    val offsetX = state.offsetX
    val scope = rememberCoroutineScope()
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnDragStopped by rememberUpdatedState(onDragStopped)

    // revealContent 随左滑距离从 0 渐变到 1
    val revealAlpha by remember {
        derivedStateOf {
            if (maxSwipePx == 0f) 0f else (-offsetX.value / maxSwipePx).coerceIn(0f, 1f)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // 右侧固定的操作区（左滑后逐渐露出）
        Box(
            modifier = Modifier
                .width(revealWidth)
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .alpha(revealAlpha)
                    .scale(revealAlpha),
                content = revealContent
            )
        }

        // 可左右拖拽的主内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        // requireUnconsumed = false：即使父级已消费 down 事件也能收到
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var isHorizontalDrag: Boolean? = null
                        var dragStarted = false
                        var accX = 0f
                        var accY = 0f
                        val slop = viewConfiguration.touchSlop

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val dx = change.position.x - change.previousPosition.x
                            val dy = change.position.y - change.previousPosition.y

                            when {
                                // 手指抬起：执行吸附动画
                                !change.pressed -> {
                                    if (dragStarted) {
                                        currentOnDragStopped()
                                        val snap = offsetX.value
                                        scope.launch {
                                            if (snap > 0f) {
                                                offsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                            } else {
                                                val target =
                                                    if (snap < -maxSwipePx * 0.5f) -maxSwipePx else 0f
                                                offsetX.animateTo(
                                                    targetValue = target,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    break
                                }

                                // 未确定方向：累积位移，超过 touchSlop 后判断方向
                                isHorizontalDrag == null -> {
                                    accX += dx
                                    accY += dy
                                    if (abs(accX) > slop || abs(accY) > slop) {
                                        isHorizontalDrag = abs(accX) >= abs(accY)
                                        if (isHorizontalDrag) {
                                            // 水平方向：消费事件，阻止父级滚动容器接管
                                            change.consume()
                                            dragStarted = true
                                            currentOnDragStarted()
                                        } else {
                                            // 垂直方向：放行，让父级处理
                                            break
                                        }
                                    }
                                }

                                // 已确定为水平拖拽：持续消费并更新偏移
                                isHorizontalDrag -> {
                                    change.consume()
                                    val newOffset =
                                        (offsetX.value + dx).coerceIn(-maxSwipePx, rightBouncePx)
                                    scope.launch { offsetX.snapTo(newOffset) }
                                }
                            }
                        }
                    }
                },
            content = content
        )
    }
}
