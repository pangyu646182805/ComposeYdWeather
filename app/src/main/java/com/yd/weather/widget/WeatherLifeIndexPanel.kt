package com.yd.weather.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.component.bounceClick
import com.yd.weather.config.Constants
import com.yd.weather.dialog.LifeIndexDetailPopup
import com.yd.weather.model.WeatherItemData
import com.yd.weather.utils.WeatherPanelClip
import kotlin.math.ceil

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WeatherLifeIndexPanel(
    item: WeatherItemData,
    index: Int = 0,
    isDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0
) {
    val weatherData = item.weatherData
    val indexes = weatherData?.indexes ?: return
    val configuration = LocalConfiguration.current
    val columnHeight = (configuration.screenWidthDp - 2 * Constants.ITEM_PANEL_MARGIN) / 3
    val rowCount = ceil(indexes.size / 3.0).toInt()
    val panelHeight = rowCount * columnHeight + Constants.ITEM_STICKY_HEIGHT
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    var showPopup by remember { mutableStateOf(false) }
    var popupIndex by remember { mutableIntStateOf(0) }

    // grid 内容区在屏幕中的位置（px）
    var gridContentXPx by remember { mutableFloatStateOf(0f) }
    var gridContentYPx by remember { mutableFloatStateOf(0f) }
    val cellSizePx = with(density) { columnHeight.dp.toPx() }

    WeatherStickyPanel(
        index = index,
        isDark = isDark,
        panelOpacity = panelOpacity,
        firstItemOffset = firstItemOffset,
        firstVisibleItemIndex = firstVisibleItemIndex,
        panelHeight = panelHeight,
        stickyTitle = "生活指数"
    ) { offsetPx, _, _ ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = panelHeight.dp)
                .clip(WeatherPanelClip(offsetPx))
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionInWindow()
                    gridContentXPx = pos.x
                    gridContentYPx = pos.y + with(density) { Constants.ITEM_STICKY_HEIGHT.dp.toPx() }
                },
            contentPadding = PaddingValues(top = Constants.ITEM_STICKY_HEIGHT.dp),
            columns = GridCells.Fixed(3),
            userScrollEnabled = false
        ) {
            itemsIndexed(indexes, key = { i, _ -> i }) { i, indexData ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(columnHeight.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WrapColumn(
                        modifier = Modifier
                            .bounceClick(
                                onClick = {
                                    popupIndex = i
                                    showPopup = true
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    popupIndex = i
                                    showPopup = true
                                }
                            )
                            .wrapContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = indexData.ext?.icon,
                            contentDescription = indexData.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(48.dp)
                        )
                        VerticalSpace(height = 8.dp)
                        AppText(
                            text = indexData.value ?: "",
                            fontSize = 13.sp,
                            color = colorResource(R.color.color_white)
                        )
                    }
                }
            }
        }
    }

    if (showPopup && popupIndex in indexes.indices) {
        LifeIndexDetailPopup(
            indexes = indexes,
            initialIndex = popupIndex,
            cellSizePx = cellSizePx,
            gridContentXPx = gridContentXPx,
            gridContentYPx = gridContentYPx,
            onDismiss = {
                showPopup = false
            }
        )
    }
}
