package com.yd.weather.utils.geo

import android.location.Location
import com.drake.logcat.LogCat
import com.yd.weather.model.LocationData
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按顺序降级的逆地理编码：系统 Geocoder 优先，失败则落到离线数据。
 *
 * 之所以不把离线放第一位：系统 Geocoder 能给到区县 + 街道，显示更细；
 * 但它在部分国行 ROM 上不可用，所以必须有个永远出得来结果的兜底。
 */
@Singleton
class ChainedGeoResolver @Inject constructor(
    private val systemGeocoderResolver: SystemGeocoderResolver,
    private val offlineCityResolver: OfflineCityResolver,
) : GeoResolver {

    override suspend fun resolve(location: Location): LocationData? {
        // Geocoder 是阻塞调用且可能长时间不返回，超时就直接降级，不能拖着 UI 转圈
        val fromSystem = withTimeoutOrNull(GEOCODER_TIMEOUT_MILLIS) {
            systemGeocoderResolver.resolve(location)
        }
        if (fromSystem != null) {
            LogCat.e("逆地理编码走系统 Geocoder: ${fromSystem.address}")
            return fromSystem
        }

        LogCat.e("系统 Geocoder 无结果，降级到离线城市数据")
        return offlineCityResolver.resolve(location)
    }

    companion object {
        private const val GEOCODER_TIMEOUT_MILLIS = 3000L
    }
}
