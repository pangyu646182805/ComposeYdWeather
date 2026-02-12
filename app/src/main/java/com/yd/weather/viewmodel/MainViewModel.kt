package com.yd.weather.viewmodel

import com.yd.weather.app.AppState
import com.yd.weather.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
) : BaseViewModel(navigator, appState) {
}