package com.yd.weather.launch

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yd.weather.R
import com.yd.weather.res.YdWeatherAppTheme
import com.yd.weather.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

/**
 * 启动页路由
 *
 * @param sharedTransitionScope 共享转换作用域
 * @param animatedContentScope 动画内容作用域
 * @param viewModel 启动页 ViewModel
 * @author Joker.X
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SplashRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: SplashViewModel = hiltViewModel()
) {
    SplashScreen()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun SplashScreen() {
    SplashContentView()
}

@OptIn(ExperimentalSharedTransitionApi::class)
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
@OptIn(ExperimentalSharedTransitionApi::class)
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
@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
internal fun SplashScreenPreviewDark() {
    YdWeatherAppTheme(darkTheme = true) {
        SplashScreen()
    }
}