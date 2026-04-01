package com.yd.weather.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 启动流程模块路由
 *
 * @author Joker.X
 */
object LaunchRoutes {
    /**
     * 启动页路由
     *
     * @author Joker.X
     */
    @Serializable
    data object Splash : NavKey
}
