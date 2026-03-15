package com.yd.weather.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.yd.weather.R
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeatherTempLineBar(
    modifier: Modifier = Modifier,
    barHeight: Dp,
    high: Int,
    low: Int,
    maxTemp: Int,
    minTemp: Int,
    temp: Int? = null
) {
    if (maxTemp == minTemp) return

    val colorCyan = colorResource(R.color.color_55dffc)
    val colorYellow = colorResource(R.color.color_eade6f)
    val colorOrange = colorResource(R.color.color_feba4f)
    val colorRed = colorResource(R.color.color_ff6f55)
    val bgColor = Color.Black.copy(alpha = 0.05f)
    val dotColor = Color.White

    val tempRange = (maxTemp - minTemp).toFloat()

    // Build gradient color stops
    val colorStops = mutableListOf<Pair<Float, Color>>()
    var startStop = 0f
    if (minTemp <= 0) {
        startStop += (0 - minTemp) / tempRange
        colorStops.add(startStop to colorCyan)
    }
    if (maxTemp > 0 && minTemp <= 15) {
        startStop += (min(maxTemp, 15) - max(minTemp, 0)) / tempRange
        colorStops.add(startStop to colorYellow)
    }
    if (maxTemp > 15 && minTemp <= 30) {
        startStop += (min(maxTemp, 30) - max(minTemp, 15)) / tempRange
        colorStops.add(startStop to colorOrange)
    }
    if (maxTemp > 30) {
        colorStops.add(1f to colorRed)
    }

    // Ensure we have at least 2 stops for the gradient
    if (colorStops.size == 1) {
        colorStops.add(0, 0f to colorStops[0].second)
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .height(barHeight)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val widthPx = size.width
            val heightPx = size.height
            val radius = heightPx / 2

            // Draw background
            drawRoundRect(
                color = bgColor,
                size = Size(widthPx, heightPx),
                cornerRadius = CornerRadius(radius, radius)
            )

            // Calculate temp bar position and width
            val tempBarWidth = max(
                ((high - low) / tempRange) * widthPx,
                heightPx
            )
            val marginLeft = ((low - minTemp) / tempRange) * widthPx

            // Clip path for the temp bar (rounded rect)
            val clipPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(marginLeft, 0f),
                            size = Size(tempBarWidth, heightPx)
                        ),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                )
            }

            // Draw gradient within clip
            clipPath(clipPath) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = colorStops.toTypedArray(),
                        startX = 0f,
                        endX = widthPx
                    ),
                    size = Size(widthPx, heightPx)
                )
            }

            // Draw current temp dot
            if (temp != null) {
                var dotLeft = (temp - minTemp) / tempRange * widthPx
                dotLeft = dotLeft.coerceIn(0f, widthPx - heightPx)
                dotLeft = dotLeft.coerceIn(marginLeft, marginLeft + tempBarWidth - heightPx)

                drawCircle(
                    color = dotColor,
                    radius = radius,
                    center = Offset(dotLeft + radius, radius)
                )
            }
        }
    }
}
