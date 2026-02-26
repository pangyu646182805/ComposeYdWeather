package com.yd.weather.main

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes

/**
 * 注册主页面路由
 *
 * @param sharedTransitionScope 共享转场作用域
 * @author Joker.X
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(sharedTransitionScope: SharedTransitionScope) {
    composable<MainRoutes.Main>(
        enterTransition = NavTransitions.Fade.enter,
        exitTransition = NavTransitions.Fade.exit,
        popEnterTransition = {
            if (initialState.destination.hasRoute<SelectCityRoutes.SelectCity>()) {
                NavTransitions.SlideHorizontal.popEnter(this)
            } else {
                NavTransitions.Fade.enter(this)
            }
        },
        popExitTransition = NavTransitions.Fade.exit,
    ) {
        MainRoute(sharedTransitionScope, this@composable)
    }
}
