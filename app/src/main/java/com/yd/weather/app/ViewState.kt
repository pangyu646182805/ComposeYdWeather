package com.yd.weather.app

sealed class ViewState {
    object Loading : ViewState()

    object Success : ViewState()

    object Error : ViewState()
}