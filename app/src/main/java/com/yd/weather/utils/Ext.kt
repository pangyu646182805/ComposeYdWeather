package com.yd.weather.utils

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
fun <T> ObserveListAddition(list: List<T>?, onAdd: () -> Unit) {
    if (list.isNullOrEmpty()) return
    var lastSize by remember { mutableIntStateOf(list.size) }

    LaunchedEffect(list.size) {
        if (list.size > lastSize) {
            onAdd()
        }
        lastSize = list.size
    }
}

@Composable
fun SetStatusBarStyle(isLight: Boolean) {
    val view = LocalView.current
    SideEffect {
        // Inside Dialog/ModalBottomSheet, use the dialog's own window
        val window = (view.parent as? DialogWindowProvider)?.window
            ?: view.context.findActivity()?.window
            ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = isLight
        }
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun Color.isLight(): Boolean {
    val luminance = luminance()
    val kThreshold = 0.15f
    if ((luminance + 0.05) * (luminance + 0.05) > kThreshold) {
        return true
    }
    return false
}
