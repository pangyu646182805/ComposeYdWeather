package com.yd.weather.utils

import android.app.Application
import com.hjq.toast.Toaster
import com.hjq.toast.style.BlackToastStyle
import com.hjq.toast.style.WhiteToastStyle

object ToastUtils {
    private var isDarkMode = false

    fun init(application: Application, isDarkTheme: Boolean = false) {
        // 保存当前主题模式
        isDarkMode = isDarkTheme

        // 根据主题选择默认样式
        val style = if (isDarkTheme) WhiteToastStyle() else BlackToastStyle()
        Toaster.init(application, style)
    }

    fun show(msg: String) {
        Toaster.show(msg)
    }
}