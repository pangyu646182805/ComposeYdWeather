package com.yd.weather.viewmodel

import androidx.lifecycle.viewModelScope
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.model.SelectCityData
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.net.ResultHandler
import com.yd.weather.net.WeatherRepository
import com.yd.weather.net.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SelectCityViewModel @Inject constructor(
    navigator: AppNavigator,
    appState: AppState,
    private val weatherRepository: WeatherRepository
) : BaseViewModel(navigator, appState) {
    init {
        loadCityList()
    }

    private val _selectCityData = MutableStateFlow<SelectCityData?>(null)
    val selectCityData: StateFlow<SelectCityData?> = _selectCityData

    fun loadCityList() {
        ResultHandler.handleResultWithData(
            scope = viewModelScope,
            flow = weatherRepository.obtainCityList().asResult(),
            onLoading = { setViewState(ViewState.Loading) },
            onData = { data ->
                setViewState(ViewState.Success)
                _selectCityData.value = data
            },
            onError = { _, _ ->
                setViewState(ViewState.Error)
            }
        )
    }
}