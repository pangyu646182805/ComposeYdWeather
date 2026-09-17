package com.yd.weather.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.yd.weather.main.mainPopEnter
import com.yd.weather.routes.CardSortRoutes
import com.yd.weather.routes.LaunchRoutes
import com.yd.weather.routes.MainRoutes
import com.yd.weather.routes.SelectCityRoutes
import com.yd.weather.routes.WeatherBgRoutes
import com.yd.weather.routes.WeatherPreviewRoutes
import com.yd.weather.selectcity.selectCityPopEnter
import com.yd.weather.selectcity.selectCityPopExit

/**
 * 手势预测返回（predictive back）的转场分派。
 *
 * ## 为什么要有这个文件
 *
 * navigation-compose 2.10.1 把 pop 拆成了两条路径：手指从屏幕边缘划回来时
 * `inPredictiveBack` 为 true，走 `predictivePop*Transition`；点返回键等其它情况才走
 * `popExitTransition`。偏偏 `composable()` **没有暴露** `predictivePop*Transition` 参数
 * （destination 级字段是 internal，源码里找不到任何赋值点），所以各页面在自己
 * `composable()` 里声明的返回动画在手势路径上根本轮不到，一律被库的默认值
 * `scaleOut(targetScale = 0.7f)` 顶掉 —— 表现就是页面缩小到 70% 然后凭空消失。
 *
 * 唯一的入口在 `NavHost` 上，且全局只有一份，所以只能在这里按页面分派。
 *
 * ## 新增页面时要做什么
 *
 * 在下面两张表里补一条，否则该页面的手势返回会落到 `else` 分支（横滑，与
 * [AppNavHost] 的全局默认一致），而不是它自己声明的那个。
 * 带条件判断的页面（城市搜索、主页）把规则提成了 internal val，两处引用同一份，
 * 不要在这里重写一遍条件。
 */
object PredictivePop {

    /** 正在退出的页面决定用哪个退场动画，对应各页面的 popExitTransition */
    val exit: PredictiveExitSpec = { _ ->
        val from = initialState.destination
        when {
            from.isBottomSheetStyle() -> NavTransitions.SlideVertical.popExit(this)
            from.hasRoute<SelectCityRoutes.SelectCity>() -> selectCityPopExit(this)
            from.hasRoute<MainRoutes.Main>() -> NavTransitions.Fade.exit(this)
            from.hasRoute<LaunchRoutes.Splash>() -> NavTransitions.Fade.exit(this)
            else -> NavTransitions.SlideHorizontal.popExit(this)
        }
    }

    /** 要回到的页面决定用哪个入场动画，对应各页面的 popEnterTransition */
    val enter: PredictiveEnterSpec = { _ ->
        val to = targetState.destination
        when {
            to.isBottomSheetStyle() -> NavTransitions.None.enter(this)
            to.hasRoute<SelectCityRoutes.SelectCity>() -> selectCityPopEnter(this)
            to.hasRoute<MainRoutes.Main>() -> mainPopEnter(this)
            to.hasRoute<LaunchRoutes.Splash>() -> NavTransitions.None.enter(this)
            else -> NavTransitions.SlideHorizontal.popEnter(this)
        }
    }

    /** 从下方滑入、向下滑出的那一类页面，四个页面的转场声明完全一致 */
    private fun NavDestination.isBottomSheetStyle(): Boolean =
        hasRoute<CardSortRoutes.CardSort>() ||
                hasRoute<WeatherPreviewRoutes.WeatherPreview>() ||
                hasRoute<WeatherBgRoutes.WeatherBgList>() ||
                hasRoute<WeatherBgRoutes.WeatherBgEdit>()
}
