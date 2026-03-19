package com.yd.weather.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.res.CommonIcon


/**
 * 居中对齐的顶部应用栏组件
 *
 * 该组件是对 Material3 CenterAlignedTopAppBar 的封装，提供了一个居中标题的顶部应用栏，
 * 支持显示返回按钮、自定义操作按钮等功能。主要用于二级页面的导航栏。
 *
 * @param title 顶部应用栏标题的资源ID，如果为null则不显示标题
 * @param titleText 顶部应用栏标题的字符串，如果为null则不显示标题
 * @param colors 顶部应用栏的颜色配置，默认使用 Material3 的居中对齐顶部应用栏颜色
 * @param actions 顶部应用栏右侧的操作按钮区域，默认为空
 * @param onBackClick 点击返回按钮时的回调函数
 * @param showBackIcon 是否显示返回按钮，默认为true
 * @author Joker.X
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopAppBar(
    modifier: Modifier = Modifier,
    title: Int? = null,
    titleText: String? = null,
    titleAlpha: Float = 1f,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable (RowScope.() -> Unit) = {},
    navigationIcon: @Composable (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    showBackIcon: Boolean = true
) {
    CenterAlignedTopAppBar(
        modifier = modifier
            .statusBarsPadding()
            .height(48.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else {
                if (showBackIcon) {
                    IconButton(onClick = onBackClick) {
                        CommonIcon(
                            resId = R.mipmap.ic_close_icon,
                            size = 22.dp,
                            tint = colorResource(R.color.black),
                        )
                    }
                }
            }
        },
        title = {
            val finalTitle = titleText ?: title?.let { stringResource(it) } ?: ""
            if (finalTitle.isNotBlank()) {
                Text(
                    text = finalTitle,
                    maxLines = 1,
                    fontSize = 18.sp,
                    color = colorResource(R.color.black).copy(alpha = titleAlpha),
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = actions,
        colors = colors
    )
}

/**
 * 居中顶部应用栏预览
 *
 * @author Joker.X
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CenterTopAppBarPreview() {
    CenterTopAppBar()
}