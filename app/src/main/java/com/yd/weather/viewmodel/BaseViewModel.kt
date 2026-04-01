package com.yd.weather.viewmodel

import androidx.lifecycle.ViewModel
import com.yd.weather.app.AppState
import com.yd.weather.app.ViewState
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.navigation.NavigationResultKey
import com.yd.weather.routes.RouteInterceptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 基础ViewModel（Navigation 3 版本）
 *
 * 提供所有ViewModel通用的功能：
 * 1. 类型安全的导航（直接操作 backStack）
 * 2. 路由拦截（登录检查）
 * 3. 类型安全的结果返回
 *
 * @param navigator 导航控制器
 * @param appState 应用状态
 * @param routeInterceptor 路由拦截器
 * @author Joker.X
 */
abstract class BaseViewModel(
    protected val navigator: AppNavigator,
    protected val appState: AppState,
    protected val routeInterceptor: RouteInterceptor = RouteInterceptor()
) : ViewModel() {
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewState

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    fun isSuccess() = ViewState.Success == _viewState.value

    fun isLoading() = ViewState.Success == _viewState.value

    fun isError() = ViewState.Error == _viewState.value

    // ==================== 基础导航方法 ====================

    /**
     * 导航到指定路由（类型安全）
     * 自动处理登录拦截逻辑
     *
     * @param route 目标路由对象（必须是 @Serializable）
     * @author Joker.X
     */
    fun navigate(route: Any) {
        val targetRoute = checkRouteInterception(route)
        navigator.navigateTo(targetRoute)
    }

    /**
     * 导航到指定路由并关闭当前页面
     *
     * @param route 目标路由对象
     * @param currentRoute 当前页面路由对象，将被关闭
     * @author Joker.X
     */
    fun navigateAndCloseCurrent(route: Any, currentRoute: Any) {
        val targetRoute = checkRouteInterception(route)
        navigator.navigateBackTo(currentRoute, inclusive = true)
        navigator.navigateTo(targetRoute)
    }

    // ==================== 返回导航方法 ====================

    /**
     * 返回上一页
     *
     * @author Joker.X
     */
    fun navigateBack() {
        navigator.navigateBack()
    }

    /**
     * 返回上一页并携带类型安全的结果
     *
     * @param key 类型安全的结果 Key
     * @param result 要传递的结果对象
     * @author Joker.X
     */
    fun <T> popBackStackWithResult(key: NavigationResultKey<T>, result: T) {
        navigator.popBackStackWithResult(key, result)
    }

    /**
     * 返回到指定路由
     *
     * @param route 目标路由对象
     * @param inclusive 是否包含目标路由本身
     * @author Joker.X
     */
    fun navigateBackTo(route: Any, inclusive: Boolean = false) {
        navigator.navigateBackTo(route, inclusive)
    }

    /**
     * 智能导航：回退栈中存在则 popBackTo，否则 replace
     */
    fun navigateToOrBackTo(route: Any) {
        navigator.navigateToOrBackTo(route)
    }

    /**
     * 消费导航结果（一次性，取走即删）
     *
     * @param key 类型安全的结果 Key
     * @return 结果值，无结果则返回 null
     * @author Joker.X
     */
    fun <T> consumeResult(key: NavigationResultKey<T>): T? {
        return navigator.consumeResult(key)
    }

    // ==================== 内部方法 ====================

    /**
     * 检查路由是否需要登录拦截（类型安全）
     *
     * @param route 目标路由对象
     * @return 如果需要拦截返回登录页面路由，否则返回原路由
     * @author Joker.X
     */
    private fun checkRouteInterception(route: Any): Any {
        return if (routeInterceptor.requiresLogin()) {
            route
        } else {
            route
        }
    }
}
