package com.yd.weather.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.WrapColumn
import com.yd.weather.component.WrapRow
import com.yd.weather.config.Constants
import com.yd.weather.db.model.CityData
import com.yd.weather.model.WeatherItemData
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.Commons
import com.yd.weather.utils.getToday
import com.yd.weather.utils.toDateString

@Composable
fun WeatherHeaderWidget(
    currentCityData: CityData? = null,
    weatherHeaderOffset: Float = 0f,
    firstVisibleItemIndex: Int = 0,
    isWeatherHeaderDark: Boolean = false,
    weatherItemData: WeatherItemData? = null,
    previewCity: Boolean = false,
    refreshOpacity: Float = 0f,
    refreshDesc: String = ""
) {
    val maxHeight = Constants.WEATHER_HEADER_MAX_HEIGHT
    val minHeight = Constants.WEATHER_HEADER_MIN_HEIGHT
    val fixedWeatherHeaderOffset =
        if (firstVisibleItemIndex == 0) weatherHeaderOffset else (maxHeight - minHeight).toFloat()
    val percent =
        fixedWeatherHeaderOffset / (maxHeight - minHeight)

    var currentHeight = maxHeight - fixedWeatherHeaderOffset
    currentHeight = if (currentHeight < minHeight) minHeight.toFloat() else currentHeight

    val maxMarginTop = 44
    val minMarginTop = 22
    var marginTop = minMarginTop + (maxMarginTop - minMarginTop) * (1 - percent)
    marginTop = if (marginTop < minMarginTop) minMarginTop.toFloat() else marginTop
    // println("weatherHeaderOffset = $weatherHeaderOffset marginTop = $marginTop currentHeight = $currentHeight")

    val opacity1 = (1 - (percent - 0.2f) / (0.3f - 0.2f)).coerceIn(0f, 1f)
    val opacity2 = (1 - (percent - 0.4f) / (0.5f - 0.4f)).coerceIn(0f, 1f)
    val opacity3 = (1 - (percent - 0.7f) / (0.8f - 0.7f)).coerceIn(0f, 1f)
    val opacity4 = (1 - (percent - 0.9f) / (0.9f - 1.0f)).coerceIn(0f, 1f)

    val textShadow = Shadow(
        color = colorResource(R.color.color_black).copy(alpha = 0.2f),
        offset = Offset(1f, 1f),
        blurRadius = 2f
    )

    val currentWeatherDetailData =
        weatherItemData?.weatherData?.forecast15?.find { it.date == getToday().toDateString(pattern = Constants.YYYY_MM_DD) }
    val weatherDesc =
        weatherItemData?.weatherData?.observe?.wthr ?: currentWeatherDetailData?.wthr ?: ""

    AppColumn(
        modifier = Modifier
            .statusBarsPadding()
            .height(currentHeight.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 下拉刷新指示器 - 参照 Dart AnimatedOpacity + AnimatedSlide
        Box(
            modifier = Modifier
                .height(marginTop.dp)
                .alpha(refreshOpacity)
                .graphicsLayer {
                    translationY = -0.2f * (1 - refreshOpacity) * marginTop
                },
            contentAlignment = Alignment.Center
        ) {
            WrapRow {
                CommonIcon(
                    resId = R.mipmap.ic_refresh_icon,
                    size = 16.dp,
                    tint = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black)
                        .copy(alpha = 0.6f)
                )
                HorizontalSpace(width = 4.dp)
                AppText(
                    text = refreshDesc,
                    fontSize = 12.sp,
                    color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black)
                        .copy(alpha = 0.6f)
                )
            }
        }
        AppText(
            modifier = Modifier.padding(horizontal = 42.dp),
            text = title(
                currentCityData = currentCityData,
                previewCity = previewCity,
                weatherItemData = weatherItemData
            ),
            fontSize = 28.sp,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = 28.sp,
                minFontSize = 22.sp,
                stepSize = 1.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
            fontWeight = FontWeight.ExtraLight,
            shadow = textShadow
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            WrapColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                AppRow(
                    modifier = Modifier.alpha(opacity3),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AppText(
                        text = weatherItemData?.weatherData?.observe?.temp?.toString() ?: "",
                        fontSize = 92.sp,
                        color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                        fontFamily = FontFamily(Font(R.font.roboto_thin, weight = FontWeight.Thin)),
                        shadow = textShadow
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        AppText(
                            text = "°",
                            fontSize = 86.sp,
                            color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                            fontFamily = FontFamily(
                                Font(R.font.roboto_thin, weight = FontWeight.Thin)
                            ),
                            shadow = textShadow
                        )
                    }
                }
                AppRow(
                    modifier = Modifier.alpha(opacity2),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AppText(
                        text = "最\n高",
                        fontSize = 15.sp,
                        color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                        fontWeight = FontWeight.ExtraLight,
                        shadow = textShadow
                    )
                    HorizontalSpace(width = 3.dp)
                    AppText(
                        text = Commons.getTemp(currentWeatherDetailData?.high),
                        fontSize = 34.sp,
                        color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                        fontFamily = FontFamily(
                            Font(
                                R.font.roboto_light,
                                weight = FontWeight.Light
                            )
                        ),
                        shadow = textShadow
                    )
                    HorizontalSpace(width = 12.dp)
                    AppText(
                        text = "最\n低",
                        fontSize = 15.sp,
                        color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                        fontWeight = FontWeight.ExtraLight,
                        shadow = textShadow
                    )
                    HorizontalSpace(width = 3.dp)
                    AppText(
                        text = Commons.getTemp(currentWeatherDetailData?.low),
                        fontSize = 34.sp,
                        color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                        fontFamily = FontFamily(
                            Font(
                                R.font.roboto_light,
                                weight = FontWeight.Light
                            )
                        ),
                        shadow = textShadow
                    )
                }
                AppText(
                    modifier = Modifier.alpha(opacity1),
                    text = weatherDesc,
                    fontSize = 20.sp,
                    color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                    fontWeight = FontWeight.ExtraLight,
                    shadow = textShadow
                )
            }
            AppText(
                modifier = Modifier.alpha(opacity4),
                text = "${Commons.getTemp(weatherItemData?.weatherData?.observe?.temp)} | $weatherDesc",
                fontSize = 20.sp,
                color = colorResource(if (isWeatherHeaderDark) R.color.color_white else R.color.color_black),
                fontWeight = FontWeight.ExtraLight,
                shadow = textShadow
            )
        }
    }
}

internal fun title(
    previewCity: Boolean = false,
    currentCityData: CityData? = null,
    weatherItemData: WeatherItemData? = null
): String {
    val cityData = if (previewCity) null else currentCityData
    val isLocationCity = cityData?.isLocationCity ?: false
    val street = cityData?.street ?: ""
    val city = weatherItemData?.weatherData?.meta?.city ?: ""
    return if (!isLocationCity || street.isEmpty()) city else "$city $street"
}