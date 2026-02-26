package com.yd.weather.selectcity

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.selectCityScreen(navController: NavHostController) {
    composable<SelectCityRoutes.SelectCity>(
        enterTransition = {
            if (initialState.destination.hasRoute<MainRoutes.Main>()) {
                NavTransitions.SlideHorizontal.enter(this)
            } else {
                NavTransitions.Fade.enter(this)
            }
        },
        exitTransition = { NavTransitions.Fade.exit(this) },
        popEnterTransition = { NavTransitions.Fade.enter(this) },
        popExitTransition = {
            if (targetState.destination.hasRoute<MainRoutes.Main>()) {
                NavTransitions.SlideHorizontal.popExit(this)
            } else {
                NavTransitions.Fade.exit(this)
            }
        },
    ) {
        val canPop = navController.previousBackStackEntry != null
        SelectCityRoute(this@composable, canPop = canPop)
    }
}