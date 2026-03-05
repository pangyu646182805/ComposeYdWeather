package com.yd.weather.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>()
    val events = _events.asSharedFlow()

    suspend fun post(event: AppEvent) {
        _events.emit(event)
    }
}