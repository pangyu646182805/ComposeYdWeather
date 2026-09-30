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

/**
 * 一镜到底：天气页在「城市管理里那张卡片」和「全屏」之间伸缩时的外轮廓。
 *
 * animValue 为 0 是全屏天气页，为 1 是列表里的城市卡片。
 *
 * 整个天气页会平移到卡片上边缘（见 [weatherCardTop]），内容钉在卡片顶上跟着走，
 * 所以这里给的是**平移之后的局部坐标**：上边缘恒为 0，只算宽和高。
 * 以前是内容不动、卡片像一扇窗在上面开合，点靠下的城市时会先露出页面下半截。
 */
class WeatherContentClip(private val animValue: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val itemHeight = with(density) { Constants.CITY_MANAGER_ITEM_HEIGHT.dp.toPx() }
        val paddingHorizontal = with(density) { 16.dp.toPx() }
        // 圆角全程保持卡片的 16dp，只在最后 10% 快铺满时收成直角。
        // 一路跟着进度线性变小的话，卡片长大时角会越来越尖，不像同一张卡片；
        // 铺满后还留着圆角，四个角又会漏出底下的城市列表
        val radius = with(density) { 16.dp.toPx() } * (animValue / 0.1f).coerceAtMost(1f)
        val rect = Rect(
            paddingHorizontal * animValue,
            0f,
            size.width - paddingHorizontal * animValue,
            size.height - (size.height - itemHeight) * animValue
        )
        return Outline.Rounded(RoundRect(rect, CornerRadius(radius, radius)))
    }
}

/** 卡片上边缘在屏幕上的位置，天气页整体平移这么多 */
fun weatherCardTop(animValue: Float, offsetY: Float): Float = offsetY * animValue

/**
 * 天气页内容（含加载/出错态）的透明度。只看进度、不分方向。
 *
 * 展开时进度走到 60%（animValue 降到 0.4）才开始淡入、90% 时完全显示；
 * 收回时反过来，走到 10% 开始淡出、40% 淡完。其余时间卡片是一张纯色的空卡片。
 * 这个节奏是从小米天气录屏逐帧量出来的，两个方向用同一条公式正好对得上。
 */
fun weatherContentAlpha(animValue: Float): Float = ((0.4f - animValue) / 0.3f).coerceIn(0f, 1f)
