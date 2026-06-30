package com.example.skycast.util

import android.util.Log

/**
 * 统一日志模块
 * - TAG 统一前缀 "SKYCAST"
 * - 生产环境将 LEVEL 设为 OFF 即可全局静默
 * - 不打印用户敏感信息（API key / 经纬度等）
 */
object SkyLog {

    /** 日志级别：VERBOSE / DEBUG / INFO / WARN / ERROR / OFF */
    var LEVEL: Int = if (com.example.skycast.BuildConfig.DEBUG) Log.DEBUG else Log.WARN

    private const val TAG = "SKYCAST"

    fun v(msg: String) { if (LEVEL <= Log.VERBOSE) Log.v(TAG, msg) }
    fun d(msg: String) { if (LEVEL <= Log.DEBUG) Log.d(TAG, msg) }
    fun i(msg: String) { if (LEVEL <= Log.INFO) Log.i(TAG, msg) }
    fun w(msg: String) { if (LEVEL <= Log.WARN) Log.w(TAG, msg) }
    fun w(msg: String, tr: Throwable) { if (LEVEL <= Log.WARN) Log.w(TAG, msg, tr) }
    fun e(msg: String) { if (LEVEL <= Log.ERROR) Log.e(TAG, msg) }
    fun e(msg: String, tr: Throwable) { if (LEVEL <= Log.ERROR) Log.e(TAG, msg, tr) }
}
