package com.yd.weather.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

private const val DURATION = 300

/**
 * Navigation 3 转场动画预设
 *
 * @author Joker.X
 */
object NavTransitions {

    // ==================== 每个 entry 的 metadata 工厂 ====================

    /** 淡入淡出 — 适用于主页、启动页等 */
    fun fadeMetadata(): Map<String, Any> =
        NavDisplay.transitionSpec {
            fadeIn(tween(DURATION)) togetherWith fadeOut(tween(DURATION))
        } + NavDisplay.popTransitionSpec {
            fadeIn(tween(DURATION)) togetherWith fadeOut(tween(DURATION))
        } + NavDisplay.predictivePopTransitionSpec { _ ->
            fadeIn(tween(DURATION)) togetherWith fadeOut(tween(DURATION))
        }

    /**
     * 上下竖滑 — 适用于底部弹出的覆盖页（CardSort、WeatherBg、WeatherPreview 等）
     *
     * 前进：新页从底部滑入，底层页面保持不动（ExitTransition.None）
     * 后退：底层页面保持不动（EnterTransition.None），当前页向下滑出
     */
    fun slideVerticalMetadata(): Map<String, Any> =
        NavDisplay.transitionSpec {
            slideInVertically(tween(DURATION)) { it } togetherWith ExitTransition.None
        } + NavDisplay.popTransitionSpec {
            EnterTransition.None togetherWith slideOutVertically(tween(DURATION)) { it }
        } + NavDisplay.predictivePopTransitionSpec { _ ->
            EnterTransition.None togetherWith slideOutVertically(tween(DURATION)) { it }
        }

    /** 无动画 — 适用于不需要过渡效果的场景 */
    fun noneMetadata(): Map<String, Any> =
        NavDisplay.transitionSpec {
            EnterTransition.None togetherWith ExitTransition.None
        } + NavDisplay.popTransitionSpec {
            EnterTransition.None togetherWith ExitTransition.None
        } + NavDisplay.predictivePopTransitionSpec { _ ->
            EnterTransition.None togetherWith ExitTransition.None
        }
}
