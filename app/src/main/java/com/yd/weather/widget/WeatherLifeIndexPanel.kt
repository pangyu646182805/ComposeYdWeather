package com.yd.weather.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.yd.weather.R
import com.yd.weather.config.Constants
import com.yd.weather.model.WeatherItemData
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
                .heightIn(max = panelHeight.dp),
            contentPadding = PaddingValues(top = Constants.ITEM_STICKY_HEIGHT.dp),
            columns = GridCells.Fixed(3),
            userScrollEnabled = false
        ) {
            items(indexes) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(columnHeight.dp)
                )
            }
        }
    }
}