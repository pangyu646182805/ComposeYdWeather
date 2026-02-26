package com.yd.weather.selectcity

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.yd.weather.component.AppScaffold
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.SearchTopAppBar
import com.yd.weather.component.WrapColumn
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.component.bounceClick
import com.yd.weather.db.model.CityData
import com.yd.weather.model.LocationData
import com.yd.weather.model.SelectCityData
import com.yd.weather.res.CommonIcon
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.utils.ToastUtils
import com.yd.weather.viewmodel.SelectCityViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SelectCityRoute(
    animatedContentScope: AnimatedContentScope,
    canPop: Boolean = false,
    viewModel: SelectCityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState by viewModel.viewState.collectAsState()
    val selectCityData by viewModel.selectCityData.collectAsState()
    val addedCities by viewModel.cities.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()

    val obtainLocationPermission = {
        viewModel.obtainLocationPermission(context)
    }
    val onBackClick = {
        viewModel.navigateBack()
    }
    val onChange = { searchKey: String ->
        if (searchKey.isEmpty()) {
            viewModel.clearSearchResult()
        } else {
            viewModel.searchCity(searchKey)
        }
    }

    LaunchedEffect(Unit) {
        obtainLocationPermission.invoke()
    }
    SelectCityScreen(
        viewState = viewState,
        selectCityData = selectCityData,
        canPop = canPop,
        addedCities = addedCities,
        locationData = locationData,
        locationState = locationState,
        obtainLocationPermission = obtainLocationPermission,
        onBackClick = onBackClick,
        onChange = onChange,
        searchResult = searchResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectCityScreen(
    viewState: ViewState = ViewState.Loading,
    selectCityData: SelectCityData? = null,
    canPop: Boolean = false,
    addedCities: List<CityData> = arrayListOf(),
    locationData: LocationData? = null,
    locationState: Int = 0,
    obtainLocationPermission: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onChange: (String) -> Unit = {},
    searchResult: List<CityData>? = null,
) {
    val focusManager = LocalFocusManager.current
    AppScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        topBar = {
            WrapColumn(modifier = Modifier.background(colorResource(R.color.bg_color))) {
                SearchTopAppBar(
                    onBackClick = onBackClick,
                    onChange = onChange,
                    onSearch = {
                        if (it.isEmpty()) ToastUtils.show("请输入搜索关键字")
                    },
                    canPop = canPop,
                )
                HorizontalDivider(thickness = 0.5.dp, color = colorResource(R.color.color_line))
            }
        }
    ) {
        MultipleStatusView(
            viewState = viewState,
        ) {
            SelectCityContent(
                selectCityData = selectCityData,
                addedCities = addedCities,
                locationData = locationData,
                locationState = locationState,
                obtainLocationPermission = obtainLocationPermission,
                searchResult = searchResult,
            )
        }
    }
}

@Composable
private fun SelectCityContent(
    selectCityData: SelectCityData? = null,
    addedCities: List<CityData>,
    locationData: LocationData? = null,
    locationState: Int = 0,
    obtainLocationPermission: () -> Unit = {},
    searchResult: List<CityData>? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SelectCityGridContent(
            selectCityData = selectCityData,
            locationData = locationData,
            locationState = locationState,
            obtainLocationPermission = obtainLocationPermission,
        )

        SelectCitySearchContent(searchResult = searchResult)
    }
}

@Composable
private fun SelectCityGridContent(
    selectCityData: SelectCityData? = null,
    locationData: LocationData? = null,
    locationState: Int = 0,
    obtainLocationPermission: () -> Unit = {},
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
        val district = locationData?.addressComponent?.district
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(top = 12.dp)) {
                WrapRow(
                    modifier = Modifier
                        .bounceClick {
                            if (locationData == null && locationState == 1) {
                                obtainLocationPermission.invoke()
                            }
                        }
                        .background(
                            colorResource(R.color.card_color_06),
                            RoundedCornerShape(percent = 50)
                        ), align = Alignment.Start, padding = 12.dp
                ) {
                    CommonIcon(
                        resId = R.mipmap.writing_icon_location1,
                        size = 18.dp,
                    )

                    VerticalDivider(thickness = 4.dp)

                    AppText(
                        text = if (locationState == 0) "定位中..." else if (district.isNullOrEmpty()) "定位失败" else district,
                        color = colorResource(R.color.text_color_01),
                        fontSize = 13.sp,
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(modifier = Modifier.padding(top = 4.dp)) {
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
private fun SelectCitySearchContent(
    searchResult: List<CityData>? = null,
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }

    AnimatedVisibility(
        visible = !searchResult.isNullOrEmpty(),
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.bg_color)),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResult?.size ?: 0) { index ->
                    SearchResultItem(searchResult?.getOrNull(index))
                }
            }
        }
    }
}

@Composable
private fun SelectCityItem(cityData: CityData) {
    AppText(
        modifier = Modifier
            .bounceClick {
                ToastUtils.show(cityData.name ?: "")
            }
            .background(
                colorResource(R.color.card_color_06),
                RoundedCornerShape(percent = 50)
            )
            .padding(vertical = 8.dp),
        text = cityData.name ?: "",
        color = colorResource(R.color.text_color_01),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SearchResultItem(item: CityData?) {
    AppText(
        modifier = Modifier
            .alphaClick() {
                ToastUtils.show(item?.name ?: "")
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        text = if (item?.prov.isNullOrEmpty()) "${item?.name} - ${item?.country}" else "${item.name} - ${item.prov} - ${item.country}",
        color = colorResource(R.color.text_color_01),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YdWeatherAppTheme {
        SelectCityScreen()
    }
}