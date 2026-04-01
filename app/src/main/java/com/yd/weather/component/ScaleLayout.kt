package com.yd.weather.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.bounceClick(
    scalePressed: Float = 1.1f,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    // 1. 定义交互状态
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 2. 自动根据状态变化的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        // 使用阻尼感更强的弹簧效果
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // 设为 null 隐藏系统自带的灰色涟漪，只保留缩放反馈
            onClick = onClick,
            onLongClick = onLongClick
        )
        .pointerInput(Unit) {
            // 3. 核心：监听底层指针事件
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(false)
                    val startTime = System.currentTimeMillis()
                    isPressed = true
                    waitForUpOrCancellation()
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val minPressTime = 100L
                    if (elapsedTime < minPressTime) {
                        // 如果按得太快，协程挂起补足剩下的时间
                        scope.launch {
                            delay(minPressTime - elapsedTime)
                            isPressed = false
                        }
                    } else {
                        isPressed = false
                    }
                }
            }
        }
}

fun Modifier.alphaClick(
    alphaPressed: Float = 0.4f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) alphaPressed else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "alphaClick"
    )

    this
        .graphicsLayer { this.alpha = alpha }
        .combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onLongClick = onLongClick,
            onClick = onClick
        )
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(false)
                    val startTime = System.currentTimeMillis()
                    isPressed = true
                    waitForUpOrCancellation()
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val minPressTime = 100L
                    if (elapsedTime < minPressTime) {
                        scope.launch {
                            delay(minPressTime - elapsedTime)
                            isPressed = false
                        }
                    } else {
                        isPressed = false
                    }
                }
            }
        }
}