package com.mori.downloader.gameturbo.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object ShellUtils {
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            ""
        }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun executeWithResult(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            val exitCode = process.waitFor()
            if (exitCode == 0) Result.success(output.trim()) else Result.failure(Exception("Exit code: $exitCode"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun executeShizuku(context: Context, command: String): String = withContext(Dispatchers.IO) {
        try {
            // Use Shizuku shell if available
            val shizukuManager = moe.shizuku.manager.ShizukuManager.getInstance(context)
            if (moe.shizuku.manager.ShizukuManager.isShizukuAvailable(context) &&
                moe.shizuku.manager.ShizukuManager.checkSelfPermission(context) == 0) {
                // Use Shizuku API
                return@withContext ""
            }
        } catch (e: Exception) {
            // Fall through to regular shell
        }
        execute(command)
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    fun killProcess(pid: Int): Boolean {
        try {
            Runtime.getRuntime().exec("kill -9 $pid").waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    @kotlinx.coroutines.jvm.JvmStatic
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("which su")
            process.waitFor() == 0
        } catch {
            false
        }
    }
}