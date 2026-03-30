package com.yd.weather.weatherbgedit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.WeatherBgRoutes

fun NavGraphBuilder.weatherBgEditScreen(navController: NavHostController) {
    composable<WeatherBgRoutes.WeatherBgEdit>(
        enterTransition = NavTransitions.SlideVertical.enter,
        exitTransition = NavTransitions.None.exit,
        popEnterTransition = NavTransitions.None.enter,
        popExitTransition = NavTransitions.SlideVertical.popExit,
    ) {
        WeatherBgEditRoute()
    }
}
