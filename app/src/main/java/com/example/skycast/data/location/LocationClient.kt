package com.example.skycast.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 后缀字符：需要从省份/城市名称末尾去掉
 */
private val SUFFIXES = listOf(
    "省", "市", "区", "县", "镇", "乡", "村",
    "自治县", "自治州", "自治区", "特别行政区",
)

private fun String.stripSuffixes(): String {
    for (s in SUFFIXES) {
        if (this.endsWith(s) && this.length > s.length) {
            return this.removeSuffix(s)
        }
    }
    return this
}

@Suppress("DEPRECATION")
private fun extractCity(geocoder: Geocoder, lat: Double, lng: Double): String {
    val addr = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull() ?: return ""
    return (addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "").stripSuffixes()
}

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
     * 获取当前设备经纬度，然后逆地理编码为省份+城市名。
     *
     * 双策略：
     * 1. 先尝试 FusedLocationProvider（真机最优）
     * 2. 失败则回退到原生 LocationManager（模拟器兼容，支持 adb/sidebar 注入）
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationResult> {
        // 策略 1: Fused（5s 超时，模拟器上常无响应）
        val fusedResult = tryFusedLocation()
        if (fusedResult.isSuccess) return fusedResult

        // 策略 2: 原生 LocationManager
        return tryNativeLocation()
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryFusedLocation(): Result<LocationResult> {
        val result = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<Result<LocationResult>> { cont ->
                try {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location == null) {
                                cont.resume(Result.failure(IOException("Fused 返回空")))
                            } else {
                                cont.resume(buildResult(location.latitude, location.longitude))
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
        return result ?: Result.failure(IOException("Fused 定位超时"))
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryNativeLocation(): Result<LocationResult> =
        withContext(Dispatchers.IO) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // 尝试 getLastKnownLocation（模拟器注入过位置就能拿到）
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER,
            )
            var best: android.location.Location? = null
            for (provider in providers) {
                try {
                    if (!locationManager.isProviderEnabled(provider)) continue
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (best == null || loc.time > best.time)) {
                        best = loc
                    }
                } catch (_: Exception) { /* 忽略单个 provider 异常 */ }
            }

            // getLastKnownLocation 没有 → 用 requestSingleUpdate 等 8s
            if (best == null) {
                best = withContext(Dispatchers.Main) {
                    requestSingleUpdateNative(locationManager)
                }
            }

            if (best != null) {
                buildResult(best.latitude, best.longitude)
            } else {
                Result.failure(IOException("无法获取位置，请在模拟器扩展控制 → Location 中设置 GPS 位置后重试"))
            }
        }

    @Suppress("DEPRECATION", "MissingPermission")
    private suspend fun requestSingleUpdateNative(
        locationManager: LocationManager,
    ): android.location.Location? {
        return try {
            withTimeoutOrNull(8000L) {
                suspendCancellableCoroutine<android.location.Location> { cont ->
                    val listener = LocationListener { location ->
                        if (cont.isActive) {
                            cont.resume(location)
                        }
                    }
                    locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        listener,
                        Looper.getMainLooper(),
                    )
                    cont.invokeOnCancellation {
                        locationManager.removeUpdates(listener)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildResult(lat: Double, lng: Double): Result<LocationResult> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val province = extractProvince(geocoder, lat, lng)
            val city = extractCity(geocoder, lat, lng)
            Result.success(
                LocationResult(
                    latitude = lat,
                    longitude = lng,
                    province = province.ifEmpty { "北京" },
                    city = city.ifEmpty { "北京" },
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
