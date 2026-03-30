package com.yd.weather.utils

import androidx.compose.ui.graphics.Color

/**
 * 从 Compose Color.value (ULong 内部编码) 安全地还原 Color。
 *
 * Compose Color(ULong) 构造函数会把 ULong 的低 10 位解读为 colorSpace ID，
 * 如果 ID 超出已注册的 colorSpace 范围（通常 ≤20），就会在 drawRect / toArgb 等
 * 调用时触发 ArrayIndexOutOfBoundsException。
 *
 * 本函数从 ULong 的高 32 位直接提取 ARGB 字节，用 Color(Int) 安全构造 sRGB 颜色。
 */
fun ULong.toSafeColor(): Color {
    val argb = (this shr 32).toInt()
    return Color(argb)
}
