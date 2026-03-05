package com.yd.weather.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppScaffold
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.viewmodel.MainViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val weatherItems by viewModel.weatherItems.collectAsStateWithLifecycle()
    val addedCities by viewModel.addedCityData.collectAsStateWithLifecycle()

    MainScreen(
        viewState = viewState,
        weatherItems = weatherItems,
        addedCities = addedCities,
        swap = { fromIndex, toIndex ->
            viewModel.swapAddedCityData(fromIndex, toIndex)
        },
        onSwapDragStopped = {
            viewModel.onSwapDragStopped()
        },
        removeCityData = { cityData, block ->
            viewModel.removeCityData(cityData, block)
        },
        removeCities = { cities, block ->
            viewModel.removeCities(cities, block)
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainScreen(
    viewState: ViewState = ViewState.Loading,
    weatherItems: List<WeatherItemData>? = null,
    addedCities: List<CityData>? = null,
    swap: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onSwapDragStopped: () -> Unit = {},
    removeCityData: (cityData: CityData?, block: () -> Unit) -> Unit = { _, _ -> },
    removeCities: (cities: List<CityData>?, block: () -> Unit) -> Unit = { _, _ -> },
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CityManagerPage(
            addedCities = addedCities,
            swap = swap,
            onSwapDragStopped = onSwapDragStopped,
            removeCityData = removeCityData,
            removeCities = removeCities
        )

        WeatherPage()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YdWeatherAppTheme {
        MainScreen()
    }
}