package com.yd.weather.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.viewmodel.SplashViewModel

/**
 * 启动页路由
 *
 * @param viewModel 启动页 ViewModel
 * @author Joker.X
 */
@Composable
internal fun SplashRoute(
    viewModel: SplashViewModel = hiltViewModel()
) {
    SplashScreen()
}

@Composable
internal fun SplashScreen() {
    SplashContentView()
}

@Composable
private fun SplashContentView() {
    Image(
        painter = painterResource(id = R.mipmap.splash),
        contentDescription = "splash",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}

/**
 * 启动页界面浅色主题预览
 *
 * @author Joker.X
 */
@Preview(showBackground = true)
@Composable
internal fun SplashScreenPreview() {
    YdWeatherAppTheme {
        SplashScreen()
    }
}

/**
 * 启动页界面深色主题预览
 *
 * @author Joker.X
 */
@Preview(showBackground = true)
@Composable
internal fun SplashScreenPreviewDark() {
    YdWeatherAppTheme(darkTheme = true) {
        SplashScreen()
    }
}