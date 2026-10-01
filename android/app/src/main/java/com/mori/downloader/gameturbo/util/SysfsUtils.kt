package com.mori.downloader.gameturbo.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object SysfsUtils {
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun write(path: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext Result.failure(Exception("Path not found: $path"))
            if (!file.canWrite()) return@withContext Result.failure(Exception("Cannot write: $path"))
            
            PrintWriter(FileWriter(file)).use { it.write(value) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun read(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext Result.failure(Exception("Path not found: $path"))
            Result.success(file.readText().trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun readInt(path: String): Result<Int> = withContext(Dispatchers.IO) {
        read(path).map { it.toIntOrNull() ?: 0 }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun readFloat(path: String): Result<Float> = withContext(Dispatchers.IO) {
        read(path).map { it.toFloatOrNull() ?: 0f }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    fun exists(path: String): Boolean = File(path).exists()
    
    @kotlinx.coroutines.jvm.JvmStatic
    fun canWrite(path: String): Boolean = File(path).canWrite()
    
    // CPU paths
    object Cpu {
        fun governorPath(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
        fun minFreqPath(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_min_freq"
        fun maxFreqPath(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq"
        fun curFreqPath(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
        fun cpuinfoMaxFreq(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq"
        fun cpuinfoMinFreq(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_min_freq"
        fun availableGovernors(core: Int) = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_available_governors"
    }
    
    // GPU paths (common)
    object Gpu {
        const val GOVERNOR_KGSL = "/sys/class/kgsl/kgsl-3d0/devfreq/governor"
        const val GOVERNOR_DEVFREQ = "/sys/class/devfreq/gpu/governor"
        const val CUR_FREQ_KGSL = "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq"
        const val MIN_FREQ_KGSL = "/sys/class/kgsl/kgsl-3d0/devfreq/min_freq"
        const val MAX_FREQ_KGSL = "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq"
        const val TEMP_KGSL = "/sys/class/kgsl/kgsl-3d0/temp"
        const val AVAILABLE_GOVERNORS_KGSL = "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors"
    }
    
    // Thermal paths
    object Thermal {
        const val ZONE_BASE = "/sys/class/thermal/thermal_zone"
        fun tempPath(zone: Int) = "$ZONE_BASE$zone/temp"
        fun typePath(zone: Int) = "$ZONE_BASE$zone/type"
        fun modePath(zone: Int) = "$ZONE_BASE$zone/mode"
    }
    
    // Scheduler paths
    object Scheduler {
        const val SCHED_BOOST = "/proc/sys/kernel/sched_boost"
        const val SCHED_UTIL_CLAMP_MIN = "/proc/sys/kernel/sched_util_clamp_min"
        const val SCHED_UTIL_CLAMP_MAX = "/proc/sys/kernel/sched_util_clamp_max"
        const val FPSGO_ENABLE = "/proc/sys/kernel/fpsgo/enable"
        const val FPSGO_BOOST_AMP = "/proc/sys/kernel/fpsgo/boost_amp"
        const val UCLAMP_MIN = "/proc/sys/kernel/sched_uclamp_min"
        const val UCLAMP_MAX = "/proc/sys/kernel/sched_uclamp_max"
    }
}