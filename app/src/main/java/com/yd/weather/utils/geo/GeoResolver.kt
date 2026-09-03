package com.yd.weather.utils.geo

import android.location.Location
import com.yd.weather.model.LocationData

/**
 * 逆地理编码：经纬度 -> 行政区划。
 *
 * 抽出这层是为了摆脱对第三方地图服务商的依赖（腾讯位置服务因商业授权被限流后不再可用）。
 * 返回 null 表示本实现解析不出结果，由调用方决定是否降级到下一顺位。
 */
interface GeoResolver {
    suspend fun resolve(location: Location): LocationData?
}
