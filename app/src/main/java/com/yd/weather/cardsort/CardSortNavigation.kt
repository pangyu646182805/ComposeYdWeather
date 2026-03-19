package com.yd.weather.cardsort

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.yd.weather.navigation.NavTransitions
import com.yd.weather.routes.CardSortRoutes

fun NavGraphBuilder.cardSortScreen(navController: NavHostController) {
    composable<CardSortRoutes.CardSort>(
        enterTransition = NavTransitions.SlideVertical.enter,
        exitTransition = NavTransitions.None.exit,
        popEnterTransition = NavTransitions.None.enter,
        popExitTransition = NavTransitions.SlideVertical.popExit,
    ) {
        CardSortRoute()
    }
}
