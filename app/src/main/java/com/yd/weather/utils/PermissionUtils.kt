package com.yd.weather.utils

import android.content.Context
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission

object PermissionUtils {
    private fun handlePermissionResult(
        activity: android.app.Activity,
        grantedList: MutableList<IPermission>,
        deniedList: MutableList<IPermission>,
        permissionName: String,
        callback: (granted: Boolean) -> Unit,
        vararg permissions: IPermission
    ) {
        val allGranted = deniedList.isEmpty()
        if (allGranted) {
            // 权限申请成功
            callback(true)
        } else {
            // 权限申请失败
            if (XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)) {
                // 用户勾选了不再询问，引导用户到设置页面
                ToastUtils.show("${permissionName}被永久拒绝，请手动授予")
                XXPermissions.startPermissionActivity(activity, *permissions)
            } else {
                ToastUtils.show("${permissionName}获取失败")
            }
            callback(false)
        }
    }

    fun requestLocationPermission(
        context: Context,
        callback: (granted: Boolean) -> Unit
    ) {
        val activity = getActivityFromContext(context)
        if (activity == null) {
            ToastUtils.show("无法获取Activity实例，权限申请失败")
            callback(false)
            return
        }

        XXPermissions.with(activity)
            .permission(PermissionLists.getAccessFineLocationPermission())
            .permission(PermissionLists.getAccessCoarseLocationPermission())
            .request { grantedList, deniedList ->
                handlePermissionResult(
                    activity,
                    grantedList,
                    deniedList,
                    "位置权限",
                    callback,
                    PermissionLists.getAccessFineLocationPermission(),
                    PermissionLists.getAccessCoarseLocationPermission()
                )
            }
    }

    private fun getActivityFromContext(context: Context): android.app.Activity? {
        return when (context) {
            is android.app.Activity -> context
            is android.content.ContextWrapper -> {
                var baseContext = context.baseContext
                while (baseContext is android.content.ContextWrapper && baseContext !is android.app.Activity) {
                    baseContext = baseContext.baseContext
                }
                baseContext as? android.app.Activity
            }

            else -> null
        }
    }
}