package com.mori.downloader.gameturbo.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.model.CPUStats
import com.mori.downloader.gameturbo.model.GPUStats
import com.mori.downloader.gameturbo.model.MemoryStats
import com.mori.downloader.gameturbo.model.ThermalInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayService : Service() {
    
    @Inject lateinit var gameTurboManager: GameTurboManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    override fun onCreate() {
        super.onCreate()
        createOverlay()
        startMonitoring()
    }
    
    private fun createOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(com.mori.downloader.R.layout.overlay_gameturbo, null)
        
        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 200 // Default position
        }
        
        overlayView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastTouchX
                    val dy = event.rawY - lastTouchY
                    windowParams?.let { params ->
                        params.x = (params.x - dx).toInt()
                        params.y = (params.y + dy).toInt()
                        windowManager?.updateViewLayout(overlayView, params)
                    }
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    true
                }
                else -> false
            }
        }
        
        try {
            overlayView?.let { view ->
                windowManager?.addView(view, windowParams)
            }
        } catch (e: Exception) {
            // Permission not granted
        }
    }
    
    private fun startMonitoring() {
        scope.launch {
            while (true) {
                val stats = gameTurboManager.stats.value
                if (stats is GameTurboManager.GameStats.Live) {
                    updateOverlay(stats)
                }
                delay(1000)
            }
        }
    }
    
    private fun updateOverlay(stats: GameTurboManager.GameStats.Live) {
        overlayView?.post {
            val fpsText = overlayView?.findViewById<TextView>(com.mori.downloader.R.id.overlay_fps)
            val cpuText = overlayView?.findViewById<TextView>(com.mori.downloader.R.id.overlay_cpu)
            val gpuText = overlayView?.findViewById<TextView>(com.mori.downloader.R.id.overlay_gpu)
            val tempText = overlayView?.findViewById<TextView>(com.mori.downloader.R.id.overlay_temp)
            val ramText = overlayView?.findViewById<TextView>(com.mori.downloader.R.id.overlay_ram)
            
            fpsText?.text = "${stats.fps.toInt()} FPS"
            cpuText?.text = "CPU: ${stats.cpu.usagePercent.toInt()}% ${stats.cpu.currentFreqMHz}MHz"
            gpuText?.text = "GPU: ${stats.gpu.currentFreqMHz}MHz"
            tempText?.text = "${stats.thermal.currentTemp.toInt()}°C"
            ramText?.text = "RAM: ${formatBytes(stats.memory.usedRam)}/${formatBytes(stats.memory.totalRam)}"
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
    
    override fun onDestroy() {
        scope.coroutineContext.cancel()
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // Ignore
            }
        }
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}