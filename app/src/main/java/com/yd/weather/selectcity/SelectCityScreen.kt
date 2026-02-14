package com.yd.weather.selectcity

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppScaffold
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.ScaleLayout
import com.yd.weather.component.SearchTopAppBar
import com.yd.weather.component.WrapColumn
import com.yd.weather.model.CityData
import com.yd.weather.model.SelectCityData
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.utils.ToastUtils
import com.yd.weather.viewmodel.SelectCityViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectCityScreen(
    viewState: ViewState = ViewState.Loading,
    selectCityData: SelectCityData? = null,
) {
    val focusManager = LocalFocusManager.current
    AppScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        topBar = {
            WrapColumn {
                SearchTopAppBar(
                    onBackClick = {},
                    onSearch = {},
                )
                HorizontalDivider(thickness = 0.5.dp, color = colorResource(R.color.color_line))
            }
        }
    ) {
        MultipleStatusView(
            viewState = viewState,
        ) {
            SelectCityContent(selectCityData = selectCityData)
        }
    }
}

@Composable
private fun SelectCityContent(
    selectCityData: SelectCityData? = null,
) {
    val hotNational = selectCityData?.hotNational ?: arrayListOf()
    val hotInternational = selectCityData?.hotInternational ?: arrayListOf()
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(top = 16.dp)) {
                AppText(
                    text = "国内热门城市",
                    color = colorResource(R.color.text_color_01),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items(hotNational.size) { index ->
            SelectCityItem(hotNational[index])
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(top = 4.dp)) {
                AppText(
                    text = "国际热门城市",
                    color = colorResource(R.color.text_color_01),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items(hotInternational.size) { index ->
            SelectCityItem(hotInternational[index])
        }
    }
}

@Composable
private fun SelectCityItem(cityData: CityData) {
    ScaleLayout(
        modifier = Modifier
            .background(
                colorResource(R.color.card_color_06),
                RoundedCornerShape(percent = 50)
            )
            .padding(vertical = 8.dp),
        onClick = {
            ToastUtils.show(cityData.name ?: "")
        }
    ) {
        AppText(
            text = cityData.name ?: "",
            color = colorResource(R.color.text_color_01),
            fontSize = 13.sp
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