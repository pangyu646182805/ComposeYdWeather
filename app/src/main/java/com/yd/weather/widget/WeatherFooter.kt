package com.yd.weather.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppColumn
import com.yd.weather.component.AppText
import com.yd.weather.component.HorizontalSpace
import com.yd.weather.component.VerticalSpace
import com.yd.weather.component.WrapRow
import com.yd.weather.component.alphaClick
import com.yd.weather.res.CommonIcon

@Composable
fun WeatherFooter(sourceTitle: String?, isDark: Boolean = false, showSortCardButton: Boolean = true) {
    AppColumn(
        modifier = Modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = "天气信息来自${sourceTitle}",
            fontSize = 12.sp,
            color = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                .copy(alpha = 0.4f)
        )
        if (showSortCardButton) {
            VerticalSpace(height = 12.dp)
            WrapRow(
                modifier = Modifier
                    .alphaClick {}
                    .background(colorResource(R.color.transparent))
                    .border(
                        0.5.dp,
                        colorResource(if (isDark) R.color.color_white else R.color.color_black)
                            .copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                CommonIcon(
                    resId = R.mipmap.ic_sort_icon,
                    size = 18.dp,
                    tint = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                        .copy(alpha = 0.5f)
                )
                HorizontalSpace(width = 4.dp)
                AppText(
                    text = "卡片排序",
                    fontSize = 15.sp,
                    color = colorResource(if (isDark) R.color.color_white else R.color.color_black)
                        .copy(alpha = 0.5f)
                )
            }
        }
    }
}