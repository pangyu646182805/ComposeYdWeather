package com.yd.weather.net

import com.yd.weather.model.NetworkResponse
import com.yd.weather.utils.LogUtils
import com.yd.weather.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ResultHandler {
    fun <T> handleResult(
        scope: CoroutineScope,
        flow: Flow<Result<NetworkResponse<T>>>,
        showToast: Boolean = true,
        delayTimeMillis: Long? = null,
        onLoading: () -> Unit = {},
        onSuccess: (NetworkResponse<T>) -> Unit = {},
        onSuccessWithData: (T) -> Unit = {},
        onError: (String, Throwable?) -> Unit = { _, _ -> },
        onFinally: () -> Unit = {}
    ) {
        scope.launch {
            delayTimeMillis?.let {
                if (it > 0) delay(it)
            }
            try {
                flow.collectLatest { result ->
                    when (result) {
                        is Result.Loading -> onLoading()
                        is Result.Success -> handleSuccess(
                            response = result.data,
                            onSuccess = onSuccess,
                            onSuccessWithData = onSuccessWithData,
                            showToast = showToast,
                            onError = onError
                        )

                        is Result.Error -> handleError(
                            errorMsg = result.exception.message ?: "网络请求失败",
                            throwable = result.exception,
                            showToast = showToast,
                            onError = onError
                        )
                    }
                }
            } catch (e: Exception) {
                handleError(
                    errorMsg = "请求处理异常",
                    throwable = e,
                    showToast = showToast,
                    onError = onError
                )
            } finally {
                onFinally()
            }
        }
    }

    fun <T> handleResultWithData(
        scope: CoroutineScope,
        flow: Flow<Result<NetworkResponse<T>>>,
        delayTimeMillis: Long? = null,
        showToast: Boolean = true,
        onLoading: () -> Unit = {},
        onData: (T) -> Unit,
        onError: (String, Throwable?) -> Unit = { _, _ -> },
        onFinally: () -> Unit = {}
    ) {
        handleResult(
            scope = scope,
            flow = flow,
            delayTimeMillis = delayTimeMillis,
            showToast = showToast,
            onLoading = onLoading,
            onSuccessWithData = onData,
            onError = onError,
            onFinally = onFinally
        )
    }

    private fun <T> handleSuccess(
        response: NetworkResponse<T>,
        onSuccess: (NetworkResponse<T>) -> Unit,
        onSuccessWithData: (T) -> Unit,
        showToast: Boolean,
        onError: (String, Throwable?) -> Unit
    ) {
        onSuccess(response)
        if (response.isSuccessful) {
            val data = response.data ?: return
            onSuccessWithData(data)
        } else {
            val errorMsg = response.errorMsg ?: "未知错误"
            handleError(errorMsg, Exception(errorMsg), showToast, onError)
        }
    }

    private fun handleError(
        errorMsg: String,
        throwable: Throwable?,
        showToast: Boolean,
        onError: (String, Throwable?) -> Unit
    ) {
        val additionalInfo = throwable?.let { getErrorTypeDescription(it) } ?: ""
        LogUtils.e(formatErrorLog(errorMsg, throwable, additionalInfo))
        onError(errorMsg, throwable)
        if (showToast) {
            ToastUtils.show(errorMsg)
        }
    }

    private fun getErrorTypeDescription(throwable: Throwable): String = when (throwable) {
        is SerializationException -> buildString {
            append("JSON解析错误\n")
            append("错误位置: ${throwable.message}")
        }

        is SocketTimeoutException -> "网络连接超时"
        is UnknownHostException -> "无法解析主机地址"
        is IOException -> "网络IO异常"
        else -> "未知异常类型"
    }

    private fun formatErrorLog(
        errorMsg: String,
        throwable: Throwable?,
        additionalInfo: String = ""
    ): String = buildString {
        appendLine("=== 网络请求错误 ===")
        appendLine("错误信息: $errorMsg")
        if (additionalInfo.isNotEmpty()) {
            appendLine("附加信息: $additionalInfo")
        }
        throwable?.let {
            appendLine("异常类型: ${it.javaClass.name}")
            appendLine("异常堆栈:")
            appendLine(getStackTraceString(it))
        }
        appendLine("==================")
    }

    private fun getStackTraceString(throwable: Throwable): String {
        return StringWriter().apply {
            throwable.printStackTrace(PrintWriter(this))
        }.toString()
    }
}