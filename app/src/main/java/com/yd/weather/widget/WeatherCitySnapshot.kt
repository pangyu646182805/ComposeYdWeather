package com.yd.weather.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.yd.weather.component.VerticalSpace
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import kotlin.math.roundToInt

/**
 * 天气城市快照（参照鸿蒙 WeatherCitySnapshot）
 *
 * 始终以全屏尺寸测量内部内容，通过 scale 整体缩放。
 * 向父布局报告缩放后的尺寸，避免溢出被裁剪。
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WeatherCitySnapshot(
    modifier: Modifier = Modifier,
    cityData: CityData?,
    weatherItems: List<WeatherItemData>?,
    weatherBg: List<Color>,
    isDark: Boolean = false,
    isWeatherHeaderDark: Boolean = false,
    panelOpacity: Float = 0.1f,
    itemTypeObserves: Array<Int>? = null,
    scale: Float = 1f
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val fullWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val fullHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val weatherItemsFilter =
        weatherItems?.filter { it.itemType != Constants.ITEM_TYPE_WEATHER_HEADER }
    val weatherHeaderItemData =
        weatherItems?.find { it.itemType == Constants.ITEM_TYPE_WEATHER_HEADER }

    // 以全屏尺寸测量，缩放后报告给父布局缩放后的尺寸
    Box(
        modifier = modifier
            .layout { measurable, _ ->
                // 以全屏尺寸约束测量子内容
                val constraints = Constraints.fixed(fullWidthPx, fullHeightPx)
                val placeable = measurable.measure(constraints)
                // 向父布局报告缩放后的尺寸
                val scaledWidth = (fullWidthPx * scale).roundToInt()
                val scaledHeight = (fullHeightPx * scale).roundToInt()
                layout(scaledWidth, scaledHeight) {
                    // 居中放置（偏移量补偿缩放差）
                    val offsetX = (scaledWidth - fullWidthPx) / 2
                    val offsetY = (scaledHeight - fullHeightPx) / 2
                    placeable.place(offsetX, offsetY)
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = weatherBg.ifEmpty { listOf(Color.Gray, Color.DarkGray) }
                )
            )
    ) {
        // 面板列表
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    top = Constants.WEATHER_HEADER_MIN_HEIGHT.dp,
                    start = Constants.ITEM_PANEL_MARGIN.dp,
                    end = Constants.ITEM_PANEL_MARGIN.dp
                )
        ) {
            if (!weatherItemsFilter.isNullOrEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = Constants.ITEM_PANEL_RADIUS.dp,
                                topEnd = Constants.ITEM_PANEL_RADIUS.dp
                            )
                        ),
                    contentPadding = PaddingValues(
                        top = (Constants.WEATHER_HEADER_MAX_HEIGHT - Constants.WEATHER_HEADER_MIN_HEIGHT).dp
                    ),
                    userScrollEnabled = false
                ) {
                    itemsIndexed(
                        weatherItemsFilter,
                        key = { _, item -> item.itemType }
                    ) { index, item ->
                        when (item.itemType) {
                            Constants.ITEM_TYPE_ALARMS -> WeatherAlarmsPanel(
                                item = item, index = index,
                                isDark = isDark, panelOpacity = panelOpacity,
                                enable = false
                            )

                            Constants.ITEM_TYPE_AIR_QUALITY -> WeatherAirQualityPanel(
                                item = item, index = index,
                                isDark = isDark, panelOpacity = panelOpacity,
                                enable = false
                            )

                            Constants.ITEM_TYPE_HOUR_WEATHER -> WeatherHourPanel(
                                item = item, index = index,
                                isDark = isDark, panelOpacity = panelOpacity,
                                enable = false
                            )

                            Constants.ITEM_TYPE_DAILY_WEATHER -> WeatherDailyPanel(
                                item = item, index = index,
                                isDark = isDark, panelOpacity = panelOpacity,
                                enable = false
                            )

                            Constants.ITEM_TYPE_OBSERVE -> WeatherObservePanel(
                                item = item, itemTypeObserves = itemTypeObserves,
                                index = index, isDark = isDark,
                                isWeatherHeaderDark = isWeatherHeaderDark,
                                panelOpacity = panelOpacity,
                                enable = false
                            )

                            Constants.ITEM_TYPE_LIFE_INDEX -> WeatherLifeIndexPanel(
                                item = item, index = index,
                                isDark = isDark, panelOpacity = panelOpacity
                            )
                        }
                        VerticalSpace(height = 12.dp)
                    }
                }
            }
        }

        // 头部
        WeatherHeaderWidget(
            currentCityData = cityData,
            isWeatherHeaderDark = isWeatherHeaderDark,
            weatherItemData = weatherHeaderItemData
        )
    }
}
