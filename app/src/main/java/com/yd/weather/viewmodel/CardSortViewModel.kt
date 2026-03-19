package com.yd.weather.viewmodel

import com.yd.weather.app.AppState
import com.yd.weather.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardSortViewModel @Inject constructor(
    navigator: AppNavigator,
    private val _appState: AppState
) : BaseViewModel(navigator, _appState) {
    fun getWeatherCardSort(): List<Int> =
        _appState.currentWeatherCardSort.value.toList()

    fun getObserveCardSort(): List<Int> =
        _appState.currentWeatherObservesCardSort.value.toList()

    fun saveWeatherCardSort(sort: IntArray) {
        _appState.setCurrentWeatherCardSort(sort.toTypedArray())
    }

    fun saveObserveCardSort(sort: IntArray) {
        _appState.setCurrentWeatherObservesCardSort(sort.toTypedArray())
    }
}
