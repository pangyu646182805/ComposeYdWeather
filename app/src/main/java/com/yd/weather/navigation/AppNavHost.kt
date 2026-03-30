package com.yd.weather.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.yd.weather.launch.launchGraph
import com.yd.weather.main.mainGraph
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.selectcity.selectCityGraph
import com.yd.weather.cardsort.cardSortScreen
import com.yd.weather.weatherbgedit.weatherBgEditScreen
import com.yd.weather.weatherbglist.weatherBgListScreen
import com.yd.weather.weatherpreview.weatherPreviewGraph
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
            // 全局默认：横向滑动。各页面可在自己的 composable() 中覆盖
            enterTransition = NavTransitions.SlideHorizontal.enter,
            exitTransition = NavTransitions.SlideHorizontal.exit,
            popEnterTransition = NavTransitions.SlideHorizontal.popEnter,
            popExitTransition = NavTransitions.SlideHorizontal.popExit,
        ) {
            mainGraph(
                navController,
                this@SharedTransitionLayout
            )
            selectCityGraph(navController)
            weatherPreviewGraph(navController)
            cardSortScreen(navController)
            weatherBgListScreen(navController)
            weatherBgEditScreen(navController)
            launchGraph(navController, this@SharedTransitionLayout)
        }
    }
}