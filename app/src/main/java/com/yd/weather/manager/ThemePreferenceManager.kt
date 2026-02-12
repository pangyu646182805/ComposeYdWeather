package com.yd.weather.manager

import com.yd.weather.config.ThemePreference
import com.yd.weather.utils.MMKVUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemePreferenceManager {
    private const val KEY_THEME_MODE = "settings_theme_mode"

    /**
     * 主题模式
     */
    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemePreference> = _themeMode.asStateFlow()

    /**
     * 更新主题模式，同时写入 MMKV
     * @param mode 新的主题模式
     */
    fun updateThemeMode(mode: ThemePreference) {
        if (_themeMode.value == mode) return
        _themeMode.value = mode
        runCatching {
            MMKVUtils.putString(KEY_THEME_MODE, mode.storageValue)
        }
    }

    /**
     * 从本地读取主题配置，异常时回退为跟随系统
     *
     * @return 读取到的主题模式
     */
    private fun readThemeMode(): ThemePreference {
        val storedValue = runCatching {
            MMKVUtils.getString(KEY_THEME_MODE, ThemePreference.FOLLOW_SYSTEM.storageValue)
        }.getOrDefault(ThemePreference.FOLLOW_SYSTEM.storageValue)
        return ThemePreference.fromStorage(storedValue)
    }
}