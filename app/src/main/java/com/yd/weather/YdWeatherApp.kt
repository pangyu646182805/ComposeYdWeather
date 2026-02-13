package com.yd.weather

import android.app.Application
import android.content.res.Configuration
import com.yd.weather.utils.LogUtils
import com.yd.weather.utils.MMKVUtils
import com.yd.weather.utils.ToastUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YdWeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initToast()
        initLog()
        initMMKV()
    }

    private fun initToast() {
        // 检测当前是否为深色模式
        val isDarkTheme = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        // 初始化Toast，传递深色模式参数
        ToastUtils.init(this, isDarkTheme)
    }

    private fun initLog() {
        LogUtils.init(true)
    }

    private fun initMMKV() {
        MMKVUtils.init(this)
    }
}