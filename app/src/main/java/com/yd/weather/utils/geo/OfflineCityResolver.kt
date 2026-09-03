package com.yd.weather.utils.geo

import android.content.Context
import android.location.Location
import com.drake.logcat.LogCat
import com.yd.weather.model.AddressComponentData
import com.yd.weather.model.LocationData
import com.yd.weather.utils.CoordinateConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 离线逆地理编码兜底：在内置的市级中心点里找最近的一个。
 *
 * 完全离线，不依赖任何地图服务商，因此不会再被配额或商业授权掐断。
 *
 * 为什么只做到市级而不是区县级：实测拿区县中心点做最近邻，在主城区的准确率只有
 * 5/8——市中心的区又小又密，中心点间距和区半径是同一量级，"最近的中心点"经常
 * 落在隔壁区。换成市级后同一批用例 17/17 全中（含临安、崇明、昆山等边界地带）。
 * 天气数据本身是市级的，同市各区无差别，所以宁可粗一档也不要给出错误的区名。
 */
@Singleton
class OfflineCityResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
) : GeoResolver {

    @Serializable
    private data class CityEntry(
        val n: String,  // 市名，写法与天气城市库一致，便于 searchCity 精确匹配
        val p: String,  // 省名
        val x: Double,  // 经度(GCJ-02)
        val y: Double,  // 纬度(GCJ-02)
    )

    private val mutex = Mutex()

    @Volatile
    private var cities: List<CityEntry>? = null

    override suspend fun resolve(location: Location): LocationData? {
        val list = loadCities()
        if (list.isNullOrEmpty()) return null

        // 内置数据是 GCJ-02，系统定位给的是 WGS84，必须先纠偏再比距离。
        // 实测不纠偏会把准确率从 5/8 拉低到 3/8。
        val (lng, lat) = CoordinateConverter.wgs84ToGcj02(location.longitude, location.latitude)
            .let { it[0] to it[1] }

        var best: CityEntry? = null
        var bestDistance = Double.MAX_VALUE
        list.forEach { entry ->
            val distance = distanceInMeters(lat, lng, entry.y, entry.x)
            if (distance < bestDistance) {
                bestDistance = distance
                best = entry
            }
        }

        val match = best ?: return null
        LogCat.e("离线匹配到 ${match.p}${match.n}，距中心点 ${bestDistance.toInt()}m")
        return LocationData(
            address = "${match.p}${match.n}",
            addressComponent = AddressComponentData(
                nation = "中国",
                province = match.p,
                city = match.n,
                // 下游拿 district 去 searchCity 匹配城市库，这里填市名
                district = match.n,
                // 离线数据没有街道级信息，UI 侧已有 street 为空时的降级显示
                street = null,
            )
        )
    }

    private suspend fun loadCities(): List<CityEntry>? {
        cities?.let { return it }
        return mutex.withLock {
            cities?.let { return@withLock it }
            val loaded = withContext(Dispatchers.IO) {
                try {
                    val text = context.assets.open(ASSET_NAME)
                        .bufferedReader()
                        .use { it.readText() }
                    json.decodeFromString<List<CityEntry>>(text)
                } catch (e: Exception) {
                    LogCat.e("离线城市数据加载失败: ${e.message}")
                    null
                }
            }
            cities = loaded
            loaded
        }
    }

    /**
     * haversine 球面距离(米)。
     * 不能用平面欧氏距离——经度间距随纬度收缩，在北方会明显偏。
     */
    private fun distanceInMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }

    companion object {
        private const val ASSET_NAME = "cities.json"
        private const val EARTH_RADIUS_METERS = 6371000.0
    }
}
