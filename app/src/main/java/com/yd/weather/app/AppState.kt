package com.yd.weather.app

import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppState @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

}