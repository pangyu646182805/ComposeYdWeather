package com.yd.weather.utils

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yd.weather.config.Constants

class WeatherContentClip(private val animValue: Float, private val offsetY: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val itemHeight = with(density) { Constants.CITY_MANAGER_ITEM_HEIGHT.dp.toPx() }
        val radius = with(density) { 16.dp.toPx() }
        val paddingHorizontal = with(density) { 16.dp.toPx() }
        val rect = Rect(
            paddingHorizontal * animValue,
            offsetY * animValue,
            size.width - paddingHorizontal * animValue,
            size.height - (size.height - offsetY - itemHeight) * animValue
        )
        val roundRect = RoundRect(rect, CornerRadius(radius * animValue, radius * animValue))
        return Outline.Rounded(roundRect)
    }
}