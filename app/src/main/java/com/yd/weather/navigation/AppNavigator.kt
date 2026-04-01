package com.yd.weather.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.yd.weather.routes.LaunchRoutes
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导航管理器 (Navigation 3)
 *
 * 直接管理 backStack（SnapshotStateList），替代 Nav2 的 NavController。
 * ViewModel 通过 AppNavigator 操作 backStack，NavDisplay 自动响应变化。
 *
 * @author Joker.X
 */
@Singleton
class AppNavigator @Inject constructor() {

    /** Nav3 back stack — NavDisplay 直接消费此列表 */
    val backStack: SnapshotStateList<Any> = mutableStateListOf(LaunchRoutes.Splash as Any)

    // ==================== 结果传递 ====================

    /** 待消费的导航结果（key → result），popBackStackWithResult 写入，consumeResult 消费 */
    private val _pendingResults = mutableMapOf<String, Any>()

    /**
     * 消费指定 key 的导航结果（一次性，取走即删）
     */
    fun <T> consumeResult(key: NavigationResultKey<T>): T? {
        val raw = _pendingResults.remove(key.key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return key.deserialize(raw)
    }

    // ==================== 导航操作 ====================

    /**
     * 导航到指定路由
     */
    fun navigateTo(route: Any) {
        backStack.add(route)
    }

    /**
     * 返回上一页
     */
    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            return true
        }
        return false
    }

    /**
     * 返回上一页并携带类型安全的结果
     */
    fun <T> popBackStackWithResult(key: NavigationResultKey<T>, result: T) {
        _pendingResults[key.key] = result as Any
        navigateBack()
    }

    /**
     * 返回到指定路由
     *
     * @param route 目标路由对象
     * @param inclusive 是否包含目标路由本身
     */
    fun navigateBackTo(route: Any, inclusive: Boolean = false) {
        val index = backStack.indexOfLast { it::class == route::class }
        if (index >= 0) {
            val removeUntil = if (inclusive) index else index + 1
            while (backStack.size > removeUntil) {
                backStack.removeLast()
            }
        }
    }

    /**
     * 智能导航：回退栈中存在目标路由则 popBackTo，否则 replace（清空栈并导航）
     */
    fun navigateToOrBackTo(route: Any) {
        val index = backStack.indexOfLast { it::class == route::class }
        if (index >= 0) {
            while (backStack.size > index + 1) {
                backStack.removeLast()
            }
        } else {
            backStack.clear()
            backStack.add(route)
        }
    }

    /**
     * 替换当前页面（先移除栈顶，再添加新页面）
     */
    fun navigateAndReplace(route: Any) {
        if (backStack.isNotEmpty()) {
            backStack.removeLast()
        }
        backStack.add(route)
    }
}
