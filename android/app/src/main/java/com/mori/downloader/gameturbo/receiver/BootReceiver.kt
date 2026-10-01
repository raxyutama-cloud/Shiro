package com.mori.downloader.gameturbo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mori.downloader.gameturbo.service.BoostService
import com.mori.downloader.gameturbo.service.GameMonitorService
import com.mori.downloader.gameturbo.service.OverlayService
import javax.inject.Inject
import dagger.hilt.android.EntryPoint
import dagger.hilt.android.EntryPointAccessors

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            
            // Start services
            val boostIntent = Intent(context, BoostService::class.java)
            context.startForegroundService(boostIntent)
            
            val monitorIntent = Intent(context, GameMonitorService::class.java)
            context.startForegroundService(monitorIntent)
            
            // Overlay service (if enabled in settings)
            val overlayIntent = Intent(context, OverlayService::class.java)
            context.startForegroundService(overlayIntent)
        }
    }
}