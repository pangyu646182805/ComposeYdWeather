package com.yd.weather.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.yd.weather.launch.launchGraph
import com.yd.weather.main.mainGraph
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.routes.MainRoutes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavHost(
    navigator: AppNavigator,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // 监听导航事件
    LaunchedEffect(navController) {
        navigator.navigationEvents.collectLatest { event ->
            navController.handleNavigationEvent(event)
        }
    }

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = LaunchRoutes.Splash,
            modifier = modifier,
            // 页面进入动画
            enterTransition = {
                val hasMainRoute = targetState.destination.hasRoute<MainRoutes.Main>()
                when {
                    hasMainRoute -> fadeIn(tween(300))
                    else -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            },
            // 页面退出动画
            exitTransition = {
                val hasMainRoute = targetState.destination.hasRoute<MainRoutes.Main>()
                when {
                    hasMainRoute -> fadeOut(tween(300))
                    else -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            },
            // 返回时页面进入动画
            popEnterTransition = {
                val hasMainRoute = targetState.destination.hasRoute<MainRoutes.Main>()
                when {
                    hasMainRoute -> fadeIn(tween(300))
                    else -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            },
            // 返回时页面退出动画
            popExitTransition = {
                val hasMainRoute = targetState.destination.hasRoute<MainRoutes.Main>()
                when {
                    hasMainRoute -> fadeOut(tween(300))
                    else -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                }
            }
        ) {
            mainGraph(
                navController,
                this@SharedTransitionLayout
            )
            launchGraph(navController, this@SharedTransitionLayout)
        }
    }
}