package com.yd.weather.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class WeatherPanelClip(private val offsetPx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = Rect(0f, offsetPx, size.width, size.height)
        return Outline.Rectangle(rect)
    }
}