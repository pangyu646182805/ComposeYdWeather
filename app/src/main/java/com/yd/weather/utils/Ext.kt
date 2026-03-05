package com.yd.weather.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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