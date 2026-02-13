package com.yd.weather.utils

import com.drake.logcat.LogCat

object LogUtils {
    fun init(isDebug: Boolean = false) {
        LogCat.setDebug(isDebug)
    }

    fun e(msg: String) {
        LogCat.e(msg)
    }
}