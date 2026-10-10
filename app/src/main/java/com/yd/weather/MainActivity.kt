package com.yd.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yd.weather.app.AppState
import com.yd.weather.config.ThemePreference
import com.yd.weather.manager.ThemePreferenceManager
import com.yd.weather.navigation.AppNavHost
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.res.YdWeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigator: AppNavigator

    @Inject
    lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        // 启动页
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemePreferenceManager.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                ThemePreference.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            YdWeatherAppTheme(darkTheme = isDarkTheme) {
                AppNavHost(navigator = navigator)
            }
        }
        // 不让启动界面一直显示
        splashScreen.setKeepOnScreenCondition {
            false
        }

        onBackPressedDispatcher.addCallback { finish() }

        // 天气背景按时段换色：在前台时时钟每分钟走一次，退到后台就停，不耗电
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { appState.runClock() }
        }
    }
}