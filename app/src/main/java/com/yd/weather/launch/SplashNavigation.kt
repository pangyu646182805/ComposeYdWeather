package com.yd.weather.launch

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.LaunchRoutes

/**
 * 启动页面导航
 *
 * @param sharedTransitionScope 共享转换作用域
 * @author Joker.X
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.splashScreen(sharedTransitionScope: SharedTransitionScope) {
    composable<LaunchRoutes.Splash>(
        enterTransition = NavTransitions.None.enter,
        exitTransition = NavTransitions.Fade.exit,
        popEnterTransition = NavTransitions.None.enter,
        popExitTransition = NavTransitions.Fade.exit,
    ) {
        SplashRoute(sharedTransitionScope, this@composable)
    }
}
