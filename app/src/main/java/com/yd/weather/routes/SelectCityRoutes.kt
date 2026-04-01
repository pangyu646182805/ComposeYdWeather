package com.yd.weather.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object SelectCityRoutes {
    @Serializable
    data object SelectCity : NavKey
}