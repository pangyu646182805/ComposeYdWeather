package com.yd.weather.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherItemData

@Composable
fun WeatherObservePanel(
    item: WeatherItemData,
    itemTypeObserves: Array<Int>?,
    index: Int = 0,
    isDark: Boolean = false,
    isWeatherHeaderDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    firstItemOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    enable: Boolean = true,
    showHideWeatherContent: ((Boolean) -> Unit)? = null
) {
    if (itemTypeObserves.isNullOrEmpty()) return
    val fixedFirstItemOffset = when {
        index + 1 > firstVisibleItemIndex -> 0f
        index + 1 == firstVisibleItemIndex -> firstItemOffset
        else -> Constants.ITEM_OBSERVE_PANEL_HEIGHT.toFloat()
    }
    val fixedFirstVisibleItemIndex =
        (fixedFirstItemOffset / (Constants.ITEM_OBSERVE_PANEL_HEIGHT + 12)).toInt() + firstVisibleItemIndex
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = (Constants.ITEM_OBSERVE_PANEL_HEIGHT * 4 + 3 * 12).dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        itemsIndexed(itemTypeObserves) { observeIndex, itemType ->
            val row = observeIndex / 2
            val fixedIndex = row + index
            val fixedFirstItemOffset = firstItemOffset - row * (Constants.ITEM_OBSERVE_PANEL_HEIGHT + 12)
            when (itemType) {
                Constants.ITEM_TYPE_OBSERVE_UV -> WeatherObserveUvPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_SHI_DU -> WeatherObserveShiDuPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_TI_GAN -> WeatherObserveTiGanPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_WD -> WeatherObserveWdPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_SUNRISE_SUNSET -> WeatherObserveSunriseSunsetPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_PRESSURE -> WeatherObservePressurePanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_VISIBILITY -> WeatherObserveVisibilityPanel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex
                )

                Constants.ITEM_TYPE_OBSERVE_FORECAST40 -> WeatherObserveForecase40Panel(
                    item = item,
                    index = fixedIndex,
                    isDark = isDark,
                    isWeatherHeaderDark = isWeatherHeaderDark,
                    panelOpacity = panelOpacity,
                    firstItemOffset = fixedFirstItemOffset,
                    firstVisibleItemIndex = fixedFirstVisibleItemIndex,
                    showHideWeatherContent = if (enable) showHideWeatherContent else null
                )
            }
        }
    }
}