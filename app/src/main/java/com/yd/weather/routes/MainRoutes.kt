package com.yd.weather.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 主模块路由
 */
object MainRoutes {
    /**
     * 主框架路由
     */
    @Serializable
    data object Main : NavKey
}
