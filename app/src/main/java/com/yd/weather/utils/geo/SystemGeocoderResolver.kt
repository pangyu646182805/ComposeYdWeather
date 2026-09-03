package com.yd.weather.utils.geo

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import com.drake.logcat.LogCat
import com.yd.weather.model.AddressComponentData
import com.yd.weather.model.LocationData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 系统自带 Geocoder 逆地理编码：免费、无 key、无配额，能拿到区县 + 街道。
 *
 * 取代原先的腾讯位置服务（因「未办理商业授权」被限流，配额是账号级共享的，换 key 无效）。
 *
 * 解析失败时返回 null，调用方保持原有定位城市不变——不做粗粒度兜底，
 * 因为市级结果会覆盖掉已有的「区 + 街道」，即使用户根本没移动。
 *
 * ⚠️ Geocoder 吃的是原始 WGS84 坐标，不要传 CoordinateConverter 纠偏后的 GCJ-02 值。
 */
@Singleton
class SystemGeocoderResolver @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GeoResolver {

    override suspend fun resolve(location: Location): LocationData? {
        if (!Geocoder.isPresent()) {
            LogCat.e("系统 Geocoder 不可用")
            return null
        }
        val geocoder = Geocoder(context, Locale.CHINA)

        // Geocoder 可能长时间不返回（33+ 的回调也可能迟迟不来），必须自己兜超时，
        // 否则调用方会一直挂在「定位中」
        val address = withTimeoutOrNull(TIMEOUT_MILLIS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                awaitAddress(geocoder, location)
            } else {
                legacyAddress(geocoder, location)
            }
        }
        if (address == null) {
            LogCat.e("系统 Geocoder 超时或无结果，保持原定位城市不变")
            return null
        }

        return toLocationData(address).also {
            if (it != null) LogCat.e("逆地理编码成功: ${it.address}")
        }
    }

    /**
     * API 33+ 的异步版本。
     * 必须用它而不是同步版：同步的 getFromLocation 阻塞且不可中断，
     * withTimeoutOrNull 对它不起作用（协程取消只在挂起点生效），超时保护会形同虚设。
     * suspendCancellableCoroutine 才能真正被取消。
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun awaitAddress(geocoder: Geocoder, location: Location): Address? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                    }

                    override fun onError(errorMessage: String?) {
                        LogCat.e("系统 Geocoder 解析失败: $errorMessage")
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            )
        }

    /** API 24~32 只有阻塞版本，放 IO 线程；它不响应取消，所以上面的超时对这条路径不精确。 */
    @Suppress("DEPRECATION")
    private suspend fun legacyAddress(geocoder: Geocoder, location: Location): Address? =
        withContext(Dispatchers.IO) {
            try {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            } catch (e: Exception) {
                LogCat.e("系统 Geocoder 解析失败: ${e.message}")
                null
            }
        }

    private fun toLocationData(address: Address): LocationData? {
        val province = address.adminArea?.takeIf { it.isNotBlank() }
        val city = address.locality?.takeIf { it.isNotBlank() }
        // 国内各家 ROM 的字段填充不一致：区县多数落在 subLocality，少数落在 subAdminArea
        val district = address.subLocality?.takeIf { it.isNotBlank() }
            ?: address.subAdminArea?.takeIf { it.isNotBlank() }

        // district 是匹配城市库的关键字段，区和市都拿不到就算解析失败
        if (district == null && city == null) {
            LogCat.e("系统 Geocoder 返回的地址缺少区县信息")
            return null
        }

        return LocationData(
            address = address.getAddressLine(0),
            addressComponent = AddressComponentData(
                nation = address.countryName,
                province = province,
                city = city,
                // 城市库里也有市级条目，拿不到区就退回市
                district = district ?: city,
                street = address.thoroughfare?.takeIf { it.isNotBlank() },
            )
        )
    }

    companion object {
        private const val TIMEOUT_MILLIS = 3000L
    }
}
