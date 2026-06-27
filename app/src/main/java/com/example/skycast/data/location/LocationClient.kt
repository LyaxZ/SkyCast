package com.example.skycast.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 后缀字符：需要从省份/城市名称末尾去掉
 */
private val SUFFIXES = listOf("省", "市", "区", "县", "镇", "乡", "村",
    "自治县", "自治州", "自治区", "特别行政区")

/**
 * 去后缀，如 "四川省" → "四川"，"北京" → "北京"
 */
private fun String.stripSuffixes(): String {
    for (s in SUFFIXES) {
        if (this.endsWith(s) && this.length > s.length) {
            return this.removeSuffix(s)
        }
    }
    return this
}

/**
 * 适用于天气 API 的城市名提取
 * 优先用 locality，为空则用 adminArea
 */
@Suppress("DEPRECATION")
private fun extractCity(geocoder: Geocoder, lat: Double, lng: Double): String {
    val addr = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull() ?: return ""
    return (addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "").stripSuffixes()
}

/**
 * 适用于天气 API 的省份名提取
 */
@Suppress("DEPRECATION")
private fun extractProvince(geocoder: Geocoder, lat: Double, lng: Double): String {
    val addr = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull() ?: return ""
    return (addr.adminArea ?: "").stripSuffixes()
}

@Singleton
class LocationClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * 获取当前设备经纬度，然后逆地理编码为省份+城市名
     * 返回 LocationResult（province 和 city 已去后缀）
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationResult> =
        suspendCancellableCoroutine { cont ->
            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location == null) {
                            cont.resume(Result.failure(IOException("定位失败，未获取到位置")))
                            return@addOnSuccessListener
                        }
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val province = extractProvince(geocoder, location.latitude, location.longitude)
                            val city = extractCity(geocoder, location.latitude, location.longitude)
                            cont.resume(Result.success(LocationResult(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                province = province.ifEmpty { "北京" },
                                city = city.ifEmpty { "北京" },
                            )))
                        } catch (e: Exception) {
                            cont.resume(Result.failure(e))
                        }
                    }
                    .addOnFailureListener { e ->
                        cont.resume(Result.failure(e))
                    }
            } catch (e: Exception) {
                cont.resume(Result.failure(e))
            }
        }
}
