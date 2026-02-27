package com.yd.weather.weatherpreview

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

fun NavGraphBuilder.weatherPreviewGraph(
    navController: NavHostController,
) {
    weatherPreviewScreen(navController)
}