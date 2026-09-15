package com.yd.weather.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.yd.weather.R
import com.yd.weather.res.CommonIcon
import com.yd.weather.res.YdWeatherAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    onBackClick: () -> Unit,
    onSearch: ((String) -> Unit)? = null,
    onChange: ((String) -> Unit)? = null,
    initialSearchText: String = "",
    scrollBehavior: TopAppBarScrollBehavior? = null,
    canPop: Boolean = true,
    /** 传入后搜索框改用液态玻璃；为 null 时退回原来的纯色底（Preview 用） */
    backdrop: Backdrop? = null,
) {
    var searchText by rememberSaveable { mutableStateOf(initialSearchText) }
    val focusManager = LocalFocusManager.current
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(imeVisible) {
        if (!imeVisible) focusManager.clearFocus()
    }

    // onChange debounce：searchText 变化后等待 300ms 无新输入才回调
    LaunchedEffect(searchText) {
        delay(300L)
        onChange?.invoke(searchText)
    }

    val performSearch = {
        onSearch?.invoke(searchText)
        focusManager.clearFocus()
    }

    TopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .height(48.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            val fieldShape = RoundedCornerShape(percent = 50)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (backdrop != null) {
                            // 用 plain 版：drawBackdrop 默认会画一圈边缘高光和阴影，
                            // 小圆按钮上不明显，拉成长条后整圈都看得见，边上就发白发脏。
                            Modifier.drawPlainBackdrop(
                                backdrop = backdrop,
                                shape = { fieldShape },
                                effects = {
                                    vibrancy()
                                    blur(2f.dp.toPx())
                                    lens(8f.dp.toPx(), 16f.dp.toPx())
                                },
                            )
                        } else Modifier
                    )
                    .clip(fieldShape)
                    // 玻璃之上仍留一层半透明卡片色，维持搜索框与顶栏之间的层次；
                    // 全靠折射的话，在浅色顶栏上几乎看不出边界。
                    .background(
                        if (backdrop != null) {
                            colorResource(R.color.card_color_02).copy(alpha = 0.55f)
                        } else {
                            colorResource(R.color.card_color_02)
                        }
                    )
            ) {
                CenterRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp)
                        .padding(vertical = 8.dp),
                ) {
                    CommonIcon(
                        resId = R.mipmap.ic_search_home,
                        size = 16.dp,
                        tint = colorResource(R.color.color_999999),
                    )

                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = colorResource(R.color.text_color_01)),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { performSearch() }
                        ),
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) {
                                AppText(
                                    text = "搜索城市（中文/拼音）",
                                    color = colorResource(R.color.color_999999),
                                    fontSize = 14.sp,
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchText.isNotEmpty()) {
                        CommonIcon(
                            resId = R.mipmap.search_clear_normal,
                            size = 12.dp,
                            tint = colorResource(R.color.color_999999),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { searchText = "" },
                        )
                    }
                }
            }
        },
        actions = {
            WrapRow {
                HorizontalSpace(width = 8.dp)
                if (canPop) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)) // 关键：裁剪水波纹边界
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = "取消",
                            color = colorResource(R.color.color_999999),
                            fontSize = 14.sp,
                        )
                    }
                }
                HorizontalSpace(width = 8.dp)
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchTopAppBarPreview() {
    YdWeatherAppTheme() {
        SearchTopAppBar(
            onSearch = {},
            onBackClick = {},
            canPop = true,
        )
    }
}