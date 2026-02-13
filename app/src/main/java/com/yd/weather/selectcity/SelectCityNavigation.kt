package com.yd.weather.selectcity

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yd.weather.routes.SelectCityRoutes

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.selectCityScreen() {
    composable<SelectCityRoutes.SelectCity> {
        SelectCityRoute(this@composable)
    }
}