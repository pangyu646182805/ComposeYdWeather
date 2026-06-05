package com.yd.weather.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * 大屏（平板 / 折叠屏展开态）宽度断点。
 * 宽度 >= 该值时启用平板 List-Detail 布局；否则保持原有手机单页布局。
 */
const val EXPANDED_WIDTH_BREAKPOINT_DP = 600

/**
 * 当前是否为大屏（平板 / 折叠展开）。
 *
 * 用 [LocalConfiguration] 的 screenWidthDp 判断，零额外依赖，旋转 / 折叠时会自动重组。
 * **约束**：小于断点时一律走原手机布局，保证手机端行为完全不变。
 */
@Composable
@ReadOnlyComposable
fun isExpandedWidth(): Boolean =
    LocalConfiguration.current.screenWidthDp >= EXPANDED_WIDTH_BREAKPOINT_DP
