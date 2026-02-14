package com.yd.weather.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 封装的Row组件，预设了常用的修饰符
 *
 * @param modifier 额外的修饰符
 * @param horizontalArrangement 水平排列方式
 * @param verticalAlignment 垂直对齐方式
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun AppRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    val finalModifier = modifier
        .let { if (fillMaxWidth) it.fillMaxWidth() else it }
        .let { if (padding > 0.dp) it.padding(padding) else it }

    Row(
        modifier = finalModifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * 水平居中的Row组件
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun CenterRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 两端对齐的Row组件
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun SpaceBetweenRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 均匀分布的Row组件
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun SpaceEvenlyRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 平均分布的Row组件（间距均匀）
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun SpaceAroundRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 左对齐的Row组件
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun StartRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 右对齐的Row组件
 *
 * @param modifier 额外的修饰符
 * @param fillMaxWidth 是否填充最大宽度
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun EndRow(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    AppRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        fillMaxWidth = fillMaxWidth,
        padding = padding,
        content = content
    )
}

/**
 * 内容包裹的Row，不会填充最大宽度
 *
 * @param modifier 额外的修饰符
 * @param horizontalArrangement 水平排列方式
 * @param verticalAlignment 垂直对齐方式
 * @param padding 内边距
 * @param content 内容
 * @author Joker.X
 */
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    padding: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .let { if (padding > 0.dp) it.padding(padding) else it },
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}