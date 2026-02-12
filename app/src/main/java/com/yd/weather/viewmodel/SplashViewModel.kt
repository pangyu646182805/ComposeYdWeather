package com.yd.weather.viewmodel

import com.yd.weather.app.AppState
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.routes.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
) : BaseViewModel(navigator, appState) {
    fun toMainPage() {
        navigateAndCloseCurrent(
            route = MainRoutes.Main,
            currentRoute = LaunchRoutes.Splash
        )
    }
}