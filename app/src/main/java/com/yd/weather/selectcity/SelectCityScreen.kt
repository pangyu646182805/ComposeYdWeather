package com.yd.weather.selectcity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.navigation.NavHostController
import com.yd.weather.R
import com.yd.weather.app.ViewState
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.yd.weather.component.AppScaffold
import com.yd.weather.component.AppText
import com.yd.weather.component.MultipleStatusView
import com.yd.weather.component.LiquidGlassFadeHeight
import com.yd.weather.component.LiquidGlassScrim
import com.yd.weather.component.liquidGlassTopBarHeight
import com.yd.weather.component.SearchTopAppBar
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.component.bounceClick
import com.yd.weather.db.model.CityData
import com.yd.weather.model.LocationData
import com.yd.weather.model.SelectCityData
import com.yd.weather.res.CommonIcon
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.utils.SetStatusBarStyle
import com.yd.weather.utils.ToastUtils
import com.yd.weather.utils.rememberElasticScrollState
import com.yd.weather.viewmodel.SelectCityViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SelectCityRoute(
    navController: NavHostController,
    canPop: Boolean = false,
    viewModel: SelectCityViewModel = hiltViewModel()
) {
    SetStatusBarStyle(isLight = true)
    val context = LocalContext.current
    val viewState by viewModel.viewState.collectAsState()
    val selectCityData by viewModel.selectCityData.collectAsState()
    val addedCities by viewModel.cities.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()

    val backStackEntry = navController.currentBackStackEntry
    LaunchedEffect(backStackEntry) {
        viewModel.observeAddCityResult(backStackEntry) { addCityId ->
            val addCityData = selectCityData?.hotNational?.find { it.cityId == addCityId }
                ?: selectCityData?.hotInternational?.find { it.cityId == addCityId }
                ?: searchResult?.find { it.cityId == addCityId }
            viewModel.addCity(addCityData)
        }
    }

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
    val gotoWeatherPreviewPage = { cityData: CityData ->
        viewModel.gotoWeatherPreviewPage(cityData)
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
        searchResult = searchResult,
        gotoWeatherPreviewPage = gotoWeatherPreviewPage
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
    gotoWeatherPreviewPage: (CityData) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    // 顶栏改为浮在内容之上的玻璃层，backdrop 是它取用的背景来源
    val backdrop = rememberLayerBackdrop()

    AppScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        // 顶栏不占布局空间，交给下面的浮层，内容才能从它底下滚过去
        topBar = {}
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MultipleStatusView(
                viewState = viewState,
            ) {
                SelectCityContent(
                    modifier = Modifier.layerBackdrop(backdrop),
                    selectCityData = selectCityData,
                    addedCities = addedCities,
                    locationData = locationData,
                    locationState = locationState,
                    obtainLocationPermission = obtainLocationPermission,
                    searchResult = searchResult,
                    gotoWeatherPreviewPage = gotoWeatherPreviewPage
                )
            }

            // 必须在内容之后、且不被 layerBackdrop 包住，否则会自己模糊自己
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                // 玻璃比内容留白高出一个渐隐段
                LiquidGlassScrim(
                    backdrop = backdrop,
                    height = liquidGlassTopBarHeight() + LiquidGlassFadeHeight,
                )
                SearchTopAppBar(
                    onBackClick = onBackClick,
                    onChange = onChange,
                    onSearch = {
                        if (it.isEmpty()) ToastUtils.show("请输入搜索关键字")
                    },
                    canPop = canPop,
                    backdrop = backdrop,
                )
            }
        }
    }
}

@Composable
private fun SelectCityContent(
    modifier: Modifier = Modifier,
    selectCityData: SelectCityData? = null,
    addedCities: List<CityData>,
    locationData: LocationData? = null,
    locationState: Int = 0,
    obtainLocationPermission: () -> Unit = {},
    searchResult: List<CityData>? = null,
    gotoWeatherPreviewPage: (CityData) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        SelectCityGridContent(
            selectCityData = selectCityData,
            addedCities = addedCities,
            locationData = locationData,
            locationState = locationState,
            obtainLocationPermission = obtainLocationPermission,
            gotoWeatherPreviewPage = gotoWeatherPreviewPage,
        )

        SelectCitySearchContent(
            searchResult = searchResult,
            addedCities = addedCities,
            gotoWeatherPreviewPage = gotoWeatherPreviewPage
        )
    }
}

@Composable
private fun SelectCityGridContent(
    selectCityData: SelectCityData? = null,
    addedCities: List<CityData>,
    locationData: LocationData? = null,
    locationState: Int = 0,
    obtainLocationPermission: () -> Unit = {},
    gotoWeatherPreviewPage: (CityData) -> Unit = {},
) {
    val hotNational = selectCityData?.hotNational ?: arrayListOf()
    val hotInternational = selectCityData?.hotInternational ?: arrayListOf()
    val elastic = rememberElasticScrollState()
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .nestedScroll(elastic.connection)
            .graphicsLayer { translationY = elastic.overscrollOffset },
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // 顶栏浮在内容上，这里补出等高留白，起始位置不变但能滚到它背后
        contentPadding = PaddingValues(top = liquidGlassTopBarHeight())
    ) {
        val district = locationData?.addressComponent?.district
        item(span = { GridItemSpan(maxLineSpan) }) {
            // 分割线去掉后这里可以收紧；留一点是为了接住顶栏玻璃的渐隐段
            Box(modifier = Modifier.padding(top = 4.dp)) {
                WrapRow(
                    modifier = Modifier
                        .bounceClick(onClick = {
                            if (locationData == null && locationState == 1) {
                                obtainLocationPermission.invoke()
                            }
                        })
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
            // 上面紧挨着定位城市那颗胶囊，网格本身已有 16dp 行距，这里不再叠加
            Box {
                AppText(
                    text = "国内热门城市",
                    color = colorResource(R.color.text_color_01),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items(hotNational.size) { index ->
            SelectCityItem(
                hotNational[index],
                addedCities = addedCities,
                gotoWeatherPreviewPage = gotoWeatherPreviewPage
            )
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
            SelectCityItem(
                hotInternational[index],
                addedCities = addedCities,
                gotoWeatherPreviewPage = gotoWeatherPreviewPage
            )
        }
    }
}

@Composable
private fun SelectCitySearchContent(
    searchResult: List<CityData>? = null,
    addedCities: List<CityData>,
    gotoWeatherPreviewPage: (CityData) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val elastic = rememberElasticScrollState()

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
                    .nestedScroll(elastic.connection)
                    .graphicsLayer { translationY = elastic.overscrollOffset },
                contentPadding = PaddingValues(
                    top = liquidGlassTopBarHeight(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            ) {
                items(searchResult?.size ?: 0) { index ->
                    SearchResultItem(
                        searchResult?.getOrNull(index),
                        addedCities = addedCities,
                        gotoWeatherPreviewPage = gotoWeatherPreviewPage
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectCityItem(
    cityData: CityData,
    addedCities: List<CityData>,
    gotoWeatherPreviewPage: (CityData) -> Unit = {}
) {
    val hasAdded = addedCities.find { it.cityId == cityData.cityId } != null
    AppText(
        modifier = Modifier
            .bounceClick(onClick = {
                gotoWeatherPreviewPage(cityData)
            })
            .background(
                colorResource(R.color.card_color_06),
                RoundedCornerShape(percent = 50)
            )
            .padding(vertical = 8.dp),
        text = cityData.name ?: "",
        color = colorResource(if (hasAdded) R.color.app_main else R.color.text_color_01),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SearchResultItem(
    item: CityData?,
    addedCities: List<CityData>,
    gotoWeatherPreviewPage: (CityData) -> Unit = {}
) {
    val hasAdded = addedCities.find { it.cityId == item?.cityId } != null
    AppText(
        modifier = Modifier
            .alphaClick() {
                gotoWeatherPreviewPage(item ?: return@alphaClick)
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        text = if (item?.prov.isNullOrEmpty()) "${item?.name} - ${item?.country}" else "${item.name} - ${item.prov} - ${item.country}",
        color = colorResource(if (hasAdded) R.color.app_main else R.color.text_color_01),
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