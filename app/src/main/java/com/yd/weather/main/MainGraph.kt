package com.yd.weather.main

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * 主模块导航图
 *
 * @param navController 导航控制器
 * @param sharedTransitionScope 共享转场作用域
 * @author Joker.X
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope
) {
    mainScreen(sharedTransitionScope)
}

