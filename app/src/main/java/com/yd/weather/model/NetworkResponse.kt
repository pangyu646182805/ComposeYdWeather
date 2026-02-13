package com.yd.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val data: T? = null,
    val result: T? = null,
    val message: String? = null,
    val desc: String? = null,
) {
    val isSuccessful: Boolean
        get() = data != null || result != null

    val errorMsg: String?
        get() = message ?: desc

}