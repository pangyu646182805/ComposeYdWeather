package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yd.weather.R

@Composable
fun AirQualityBar(
    barHeight: Dp,
    aqi: Int = 0
) {
    val density = LocalDensity.current
    var marginStart by remember { mutableStateOf(0.dp) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(R.color.color_00e301),
                        colorResource(R.color.color_fdfd01),
                        colorResource(R.color.color_fd7e01),
                        colorResource(R.color.color_f70001),
                        colorResource(R.color.color_98004c),
                        colorResource(R.color.color_7d0023)
                    )
                ),
                shape = CircleShape
            )
            .onSizeChanged { size ->
                val percent = (aqi / 500f).coerceIn(0f, 1f)
                marginStart = (with(density) { size.width.toDp() } - barHeight) * percent
            }
    ) {
        Box(
            modifier = Modifier
                .padding(start = marginStart)
                .size(barHeight)
                .clip(CircleShape)
                .background(colorResource(R.color.color_white))
        )
    }
}