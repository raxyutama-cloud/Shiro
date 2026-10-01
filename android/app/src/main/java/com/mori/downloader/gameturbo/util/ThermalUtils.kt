package com.mori.downloader.gameturbo.util

import com.mori.downloader.gameturbo.model.ThermalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ThermalUtils {
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun getThermalInfo(): ThermalInfo = withContext(Dispatchers.IO) {
        var maxTemp = 0f
        var throttlingLevel = 0
        var cpuThrottled = false
        var gpuThrottled = false
        
        val thermalDir = File("/sys/class/thermal")
        if (thermalDir.exists()) {
            thermalDir.listFiles()?.forEach { zone ->
                val tempFile = File(zone, "temp")
                val typeFile = File(zone, "type")
                
                if (tempFile.exists()) {
                    try {
                        val temp = tempFile.readText().trim().toFloat() / 1000 // Convert to Celsius
                        maxTemp = maxOf(maxTemp, temp)
                        
                        val type = typeFile.readText().trim().lowercase()
                        when {
                            type.contains("cpu") -> cpuThrottled = temp > 80
                            type.contains("gpu") -> gpuThrottled = temp > 80
                            type.contains("skin") -> { /* skin temp */ }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
        
        // Determine throttling level based on max temperature
        throttlingLevel = when {
            maxTemp >= 95 -> 4 // Critical
            maxTemp >= 85 -> 3 // Severe
            maxTemp >= 75 -> 2 // Moderate
            maxTemp >= 65 -> 1 // Light
            else -> 0 // Normal
        }
        
        ThermalInfo(
            currentTemp = maxTemp,
            throttlingLevel = throttlingLevel,
            cpuThrottled = cpuThrottled,
            gpuThrottled = gpuThrottled
        )
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun isThermalThrottling(): Boolean = withContext(Dispatchers.IO) {
        val info = getThermalInfo()
        info.throttlingLevel >= 2
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    fun getThrottlingLevel(temp: Float): Int = when {
        temp >= 95 -> 4
        temp >= 85 -> 3
        temp >= 75 -> 2
        temp >= 65 -> 1
        else -> 0
    }
}