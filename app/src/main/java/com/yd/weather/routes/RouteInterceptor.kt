package com.yd.weather.routes

import kotlin.reflect.KClass

/**
 * 路由拦截器（类型安全版本）
 *
 * 负责管理需要登录的页面配置和路由拦截逻辑
 * 使用类型安全的方式处理路由拦截
 *
 * @author Joker.X
 */
class RouteInterceptor {
    /**
     * 检查指定路由对象是否需要登录
     *
     * @return true表示需要登录，false表示不需要登录
     * @author Joker.X
     */
    fun requiresLogin(): Boolean {
        return false
    }

    /**
     * 添加需要登录的路由类型
     *
     * @param routeClass 需要登录的路由类型
     * @author Joker.X
     */
    fun addLoginRequiredRoute(routeClass: KClass<*>) {
        // (loginRequiredRouteTypes as MutableSet).add(routeClass)
    }

    /**
     * 移除需要登录的路由类型
     *
     * @param routeClass 不再需要登录的路由类型
     * @author Joker.X
     */
    fun removeLoginRequiredRoute(routeClass: KClass<*>) {
        // (loginRequiredRouteTypes as MutableSet).remove(routeClass)
    }
}
