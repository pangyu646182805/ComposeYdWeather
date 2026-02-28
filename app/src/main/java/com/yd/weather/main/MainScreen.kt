package com.yd.weather.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppScaffold
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.viewmodel.MainViewModel
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val weatherItems by viewModel.weatherItems.collectAsStateWithLifecycle()
    MainScreen(
        viewState = viewState,
        weatherItems = weatherItems,
        toSelectCity = viewModel::toSelectCityPage
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MainScreen(
    viewState: ViewState = ViewState.Loading,
    weatherItems: List<WeatherItemData>? = null,
    toSelectCity: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val city = weatherItems?.getOrNull(0)?.weatherData?.meta?.city
    AppScaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Hello Compose!",
            )

            Button(shape = RoundedCornerShape(8.dp), onClick = toSelectCity) {
                Text(
                    text = "选择城市",
                )
            }

            MultipleStatusView(
                viewState = viewState,
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    AppText(text = city + " " + Json.encodeToString(weatherItems?.getOrNull(0)))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YdWeatherAppTheme {
        MainScreen()
    }
}