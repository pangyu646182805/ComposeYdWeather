package com.yd.weather.weatherbglist

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.WeatherBgRoutes

fun NavGraphBuilder.weatherBgListScreen(navController: NavHostController) {
    composable<WeatherBgRoutes.WeatherBgList>(
        enterTransition = NavTransitions.SlideVertical.enter,
        exitTransition = NavTransitions.None.exit,
        popEnterTransition = NavTransitions.None.enter,
        popExitTransition = NavTransitions.SlideVertical.popExit,
    ) {
        WeatherBgListRoute()
    }
}
