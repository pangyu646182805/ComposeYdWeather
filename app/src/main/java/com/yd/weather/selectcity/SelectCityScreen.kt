package com.yd.weather.selectcity

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.app.ViewState
import com.yd.weather.model.SelectCityData
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.viewmodel.SelectCityViewModel
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SelectCityRoute(
    animatedContentScope: AnimatedContentScope,
    viewModel: SelectCityViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val selectCityData by viewModel.selectCityData.collectAsState()
    SelectCityScreen(
        viewState = viewState,
        selectCityData = selectCityData,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SelectCityScreen(
    viewState: ViewState = ViewState.Loading,
    selectCityData: SelectCityData? = null,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        val text = when (viewState) {
            ViewState.Loading -> "Loading"
            ViewState.Success -> Json.encodeToString(selectCityData)
            ViewState.Error -> "Error"
        }
        Text(
            text = text,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YdWeatherAppTheme {
        SelectCityScreen()
    }
}