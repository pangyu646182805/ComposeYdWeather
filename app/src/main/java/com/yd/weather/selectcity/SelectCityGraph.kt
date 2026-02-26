package com.yd.weather.selectcity

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.selectCityGraph(
    navController: NavHostController,
) {
    selectCityScreen(navController)
}