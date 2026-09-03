package com.yd.weather.utils.geo

import android.location.Location
import com.yd.weather.model.LocationData

/**
 * 逆地理编码：经纬度 -> 行政区划。
 *
 * 抽出这层是为了摆脱对第三方地图服务商的依赖（腾讯位置服务因商业授权被限流后不再可用），
 * 也留下一个换实现不用动 ViewModel 的接缝。
 *
 * 返回 null 表示解析失败，调用方应保持原有定位城市不变。
 */
interface GeoResolver {
    suspend fun resolve(location: Location): LocationData?
}
