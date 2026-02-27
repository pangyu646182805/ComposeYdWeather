package com.yd.weather.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.yd.weather.app.AppState
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.routes.WeatherPreviewRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherPreviewViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(navigator, appState) {
    val cityId: String? =
        savedStateHandle.toRoute<WeatherPreviewRoutes.WeatherPreview>().cityId
}