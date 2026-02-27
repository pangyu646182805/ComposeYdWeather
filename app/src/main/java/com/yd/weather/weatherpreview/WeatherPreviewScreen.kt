package com.yd.weather.weatherpreview

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.component.AppRow
import com.yd.weather.component.AppText
import com.yd.weather.component.bounceClick
import com.yd.weather.navigation.AddCityResultKey
import com.yd.weather.viewmodel.WeatherPreviewViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun WeatherPreviewRoute(
    viewModel: WeatherPreviewViewModel = hiltViewModel()
) {
    val statusBarTop = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.color_999999))
    ) {
        AppRow(
            modifier = Modifier
                .padding(start = 12.dp, top = statusBarTop + 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FunctionButton("取消") {
                viewModel.navigateBack()
            }

            FunctionButton("添加") {
                viewModel.popBackStackWithResult(AddCityResultKey, viewModel.cityId ?: "")
            }
        }
    }
}

@Composable
fun FunctionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .height(32.dp)
            .background(
                colorResource(R.color.color_black).copy(alpha = 0.1f),
                RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = text,
            color = colorResource(R.color.color_white),
            fontSize = 14.sp,
        )
    }
}