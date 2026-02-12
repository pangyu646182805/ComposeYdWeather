package com.yd.weather.main

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yd.weather.routes.MainRoutes

/**
 * 注册主页面路由
 *
 * @param sharedTransitionScope 共享转场作用域
 * @author Joker.X
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(sharedTransitionScope: SharedTransitionScope) {
    composable<MainRoutes.Main> {
        MainRoute(sharedTransitionScope, this@composable)
    }
}
