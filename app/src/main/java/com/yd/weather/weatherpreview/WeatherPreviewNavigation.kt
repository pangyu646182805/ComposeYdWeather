package com.yd.weather.weatherpreview

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.WeatherPreviewRoutes

fun NavGraphBuilder.weatherPreviewScreen(navController: NavHostController) {
    composable<WeatherPreviewRoutes.WeatherPreview>(
        enterTransition = NavTransitions.SlideVertical.enter,
        exitTransition = NavTransitions.None.exit,
        popEnterTransition = NavTransitions.None.enter,
        popExitTransition = NavTransitions.SlideVertical.popExit,
    ) {
        WeatherPreviewRoute()
    }
}