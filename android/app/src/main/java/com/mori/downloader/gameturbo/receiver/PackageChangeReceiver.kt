package com.mori.downloader.gameturbo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.mori.downloader.gameturbo.core.GameDetector
import javax.inject.Inject
import dagger.hilt.android.EntryPoint
import dagger.hilt.android.EntryPointAccessors

class PackageChangeReceiver : BroadcastReceiver() {
    
    @Inject lateinit var gameDetector: GameDetector
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.schemeSpecificPart
        
        when (action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    // New app installed, refresh game list
                    val entryPoint = EntryPointAccessors.fromApplication(context, GameTurboEntryPoint::class.java)
                    entryPoint.gameDetector().getAllGames(true)
                }
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    // App uninstalled
                    val entryPoint = EntryPointAccessors.fromApplication(context, GameTurboEntryPoint::class.java)
                    entryPoint.gameDetector().clearCache()
                }
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                if (packageName != null) {
                    // App updated
                    val entryPoint = EntryPointAccessors.fromApplication(context, GameTurboEntryPoint::class.java)
                    entryPoint.gameDetector().clearCache()
                }
            }
        }
    }
    
    @EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface GameTurboEntryPoint {
        fun gameDetector(): GameDetector
    }
}