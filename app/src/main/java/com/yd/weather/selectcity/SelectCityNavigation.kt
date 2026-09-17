package com.yd.weather.selectcity

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.EnterSpec
import com.yd.weather.navigation.ExitSpec
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes
import com.yd.weather.routes.WeatherPreviewRoutes

/**
 * 返回本页时的转场。手势预测返回的分派表也引用它，规则只留这一份
 * （见 [com.yd.weather.navigation.PredictivePop]）
 */
internal val selectCityPopEnter: EnterSpec = {
    if (initialState.destination.hasRoute<WeatherPreviewRoutes.WeatherPreview>()) {
        NavTransitions.None.enter(this)
    } else {
        NavTransitions.Fade.enter(this)
    }
}

/** 从本页返回时的转场，同上，与手势预测返回共用 */
internal val selectCityPopExit: ExitSpec = {
    if (targetState.destination.hasRoute<MainRoutes.Main>()) {
        NavTransitions.SlideHorizontal.popExit(this)
    } else {
        NavTransitions.Fade.exit(this)
    }
}

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
        exitTransition = {
            if (targetState.destination.hasRoute<WeatherPreviewRoutes.WeatherPreview>()) {
                NavTransitions.None.exit(this)
            } else {
                NavTransitions.Fade.exit(this)
            }
        },
        popEnterTransition = selectCityPopEnter,
        popExitTransition = selectCityPopExit,
    ) {
        val canPop = navController.previousBackStackEntry != null
        SelectCityRoute(navController, canPop = canPop)
    }
}