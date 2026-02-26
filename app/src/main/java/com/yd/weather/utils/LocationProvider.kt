package com.yd.weather.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    suspend fun fetchSingleLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isNetworkEnabled) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val provider = LocationManager.NETWORK_PROVIDER

        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            null as CancellationSignal?, // 取消信号（可以用 CancellationSignal）
            { it.run() }, // Executor：在当前线程执行回调
            { location -> if (continuation.isActive) continuation.resume(location) }
        )
    }
}