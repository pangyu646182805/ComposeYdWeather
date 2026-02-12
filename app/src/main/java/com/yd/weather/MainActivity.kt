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
import com.yd.weather.config.ThemePreference
import com.yd.weather.manager.ThemePreferenceManager
import com.yd.weather.navigation.AppNavHost
import com.yd.weather.navigation.AppNavigator
import com.yd.weather.res.YdWeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigator: AppNavigator

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
    }
}