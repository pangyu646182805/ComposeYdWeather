package com.yd.weather.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.yd.weather.cardsort.CardSortRoute
import com.yd.weather.launch.SplashRoute
import com.yd.weather.main.MainRoute
import com.yd.weather.routes.CardSortRoutes
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes
import com.yd.weather.routes.WeatherBgRoutes
import com.yd.weather.routes.WeatherPreviewRoutes
import com.yd.weather.selectcity.SelectCityRoute
import com.yd.weather.weatherbgedit.WeatherBgEditRoute
import com.yd.weather.weatherbglist.WeatherBgListRoute
import com.yd.weather.weatherpreview.WeatherPreviewRoute

@Composable
fun AppNavHost(
    navigator: AppNavigator,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.navigateBack() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(tween(300)) { it } togetherWith
                    slideOutHorizontally(tween(300)) { -it }
        },
        popTransitionSpec = {
            slideInHorizontally(tween(300)) { -it } togetherWith
                    slideOutHorizontally(tween(300)) { it }
        },
        predictivePopTransitionSpec = { _ ->
            slideInHorizontally(tween(300)) { -it } togetherWith
                    slideOutHorizontally(tween(300)) { it }
        },
        entryProvider = entryProvider {
            // 启动页
            entry<LaunchRoutes.Splash>(
                metadata = NavTransitions.fadeMetadata()
            ) {
                SplashRoute()
            }

            // 主页
            entry<MainRoutes.Main>(
                metadata = NavTransitions.fadeMetadata()
            ) {
                MainRoute()
            }

            // 选择城市
            entry<SelectCityRoutes.SelectCity> {
                val canPop = navigator.backStack.size > 1
                SelectCityRoute(canPop = canPop)
            }

            // 天气预览（带参数）
            entry<WeatherPreviewRoutes.WeatherPreview>(
                metadata = NavTransitions.slideVerticalMetadata()
            ) { key ->
                WeatherPreviewRoute(cityId = key.cityId)
            }

            // 卡片排序
            entry<CardSortRoutes.CardSort>(
                metadata = NavTransitions.slideVerticalMetadata()
            ) {
                CardSortRoute()
            }

            // 天气背景列表
            entry<WeatherBgRoutes.WeatherBgList>(
                metadata = NavTransitions.slideVerticalMetadata()
            ) {
                WeatherBgListRoute()
            }

            // 天气背景编辑（带参数）
            entry<WeatherBgRoutes.WeatherBgEdit>(
                metadata = NavTransitions.slideVerticalMetadata()
            ) { key ->
                WeatherBgEditRoute(route = key)
            }
        }
    )
}
