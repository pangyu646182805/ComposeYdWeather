package com.yd.weather.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

private const val DURATION = 300

// 类型别名，让 composable() 参数声明更简洁
typealias EnterSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
typealias ExitSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

/**
 * 导航转场预设
 *
 * 用法：在各页面的 composable() 中按需引用，无需修改 AppNavHost。
 *
 * 示例：
 * ```kotlin
 * composable<SomeRoutes.SomePage>(
 *     enterTransition    = NavTransitions.Fade.enter,
 *     exitTransition     = NavTransitions.Fade.exit,
 *     popEnterTransition = NavTransitions.Fade.enter,
 *     popExitTransition  = NavTransitions.Fade.exit,
 * ) { ... }
 * ```
 *
 * @author Joker.X
 */
object NavTransitions {

    /** 淡入淡出 —— 适用于主页、全屏弹出页等 */
    object Fade {
        val enter: EnterSpec = { fadeIn(tween(DURATION)) }
        val exit: ExitSpec = { fadeOut(tween(DURATION)) }
    }

    /** 左右横滑 —— 标准 push 导航（AppNavHost 全局默认） */
    object SlideHorizontal {
        val enter: EnterSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURATION))
        }
        val exit: ExitSpec = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURATION))
        }
        val popEnter: EnterSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURATION))
        }
        val popExit: ExitSpec = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURATION))
        }
    }

    /** 上下竖滑 —— 适用于底部弹出的详情页等 */
    object SlideVertical {
        val enter: EnterSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(DURATION))
        }
        val exit: ExitSpec = { fadeOut(tween(DURATION)) }
        val popEnter: EnterSpec = { fadeIn(tween(DURATION)) }
        val popExit: ExitSpec = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(DURATION))
        }
    }

    /** 无动画 —— 适用于启动页等不需要过渡效果的场景 */
    object None {
        val enter: EnterSpec = { EnterTransition.None }
        val exit: ExitSpec = { ExitTransition.None }
    }
}
