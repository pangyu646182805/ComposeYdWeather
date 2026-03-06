package com.yd.weather.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yd.weather.app.ViewState

@Composable
fun MultipleStatusView(
    viewState: ViewState,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    loadingColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    customLoading: @Composable (() -> Unit)? = null,
    customError: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .padding(padding),
    ) {
        AnimatedContent(
            targetState = viewState,
            transitionSpec = {
                // 定义进入和退出动画
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "NetworkStateAnimation"
        ) { state ->
            when (state) {
                is ViewState.Loading -> {
                    if (customLoading != null) {
                        customLoading()
                    } else {
                        PageLoading(color = loadingColor)
                    }
                }

                is ViewState.Error -> {
                    if (customError != null) {
                        customError()
                    } else {
                        AppText(text = "出错啦！")
                    }
                }

                is ViewState.Success -> content()
            }
        }
    }
}