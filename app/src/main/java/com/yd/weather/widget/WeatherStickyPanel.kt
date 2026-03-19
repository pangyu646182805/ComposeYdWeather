package com.yd.weather.widget

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.config.Constants

@Composable
fun WeatherStickyPanel(
    modifier: Modifier = Modifier,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    panelHeight: Int,
    stickyTitle: String,
    supportTitleOpacity: Boolean = false,
    animateContentSize: Boolean = false,
    rightStickContent: @Composable () -> Unit = {},
    content: @Composable (offsetPx: Float, titleOpacity: Float, offset: Float) -> Unit
) {
    val density = LocalDensity.current
    val heightDp = if (animateContentSize) {
        val animatedHeight by animateDpAsState(
            targetValue = panelHeight.dp,
            animationSpec = tween(durationMillis = 300),
            label = "panelHeight"
        )
        animatedHeight
    } else {
        panelHeight.dp
    }
    val offset = when {
        index + 1 > firstVisibleItemIndex -> 0f
        index + 1 == firstVisibleItemIndex -> firstItemOffset
        else -> panelHeight.toFloat()
    }
    val percent =
        ((offset - (panelHeight - Constants.ITEM_STICKY_HEIGHT)) / Constants.ITEM_STICKY_HEIGHT)
            .coerceIn(0f, 1f)
    val contentOpacity = 1 - percent
    var stickyTranslateY =
        if (offset > panelHeight - Constants.ITEM_STICKY_HEIGHT)
            (panelHeight - Constants.ITEM_STICKY_HEIGHT).toFloat()
        else
            offset
    stickyTranslateY += percent * Constants.ITEM_STICKY_HEIGHT * 0.5f
    val titleOpacity = (1 - offset / 12).coerceIn(0f, 1f)
    val offsetPx = with(density) {
        if (supportTitleOpacity)
            (offset + Constants.ITEM_STICKY_HEIGHT * (offset / Constants.ITEM_STICKY_HEIGHT).coerceIn(
                0f, 1f
            )).dp.toPx()
        else
            (offset + Constants.ITEM_STICKY_HEIGHT).dp.toPx()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .alpha(contentOpacity)
            .background(
                colorResource(if (isDark) R.color.color_white else R.color.color_black).copy(alpha = panelOpacity),
                shape = RoundedCornerShape(Constants.ITEM_PANEL_RADIUS.dp)
            )
    ) {
        AppRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.ITEM_STICKY_HEIGHT.dp)
                .graphicsLayer(
                    alpha = if (supportTitleOpacity) 1 - titleOpacity else 1f,
                    translationY = with(density) { stickyTranslateY.dp.toPx() }
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText(
                modifier = Modifier
                    .height(Constants.ITEM_STICKY_HEIGHT.dp)
                    .padding(start = Constants.ITEM_PANEL_MARGIN.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                text = stickyTitle,
                fontSize = 12.sp,
                color = colorResource(R.color.color_white).copy(alpha = 0.6f),
                textAlign = TextAlign.Start
            )
            rightStickContent()
        }
        content(offsetPx, titleOpacity, offset)
    }
}
