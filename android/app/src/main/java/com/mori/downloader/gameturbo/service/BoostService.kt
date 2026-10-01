package com.mori.downloader.gameturbo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.core.ProfileManager
import com.mori.downloader.gameturbo.model.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoostService : Service() {
    
    companion object {
        const val ACTION_START_BOOST = "com.mori.downloader.gameturbo.START_BOOST"
        const val ACTION_STOP_BOOST = "com.mori.downloader.gameturbo.STOP_BOOST"
        const val EXTRA_PACKAGE = "package_name"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "gameturbo_boost"
    }
    
    @Inject lateinit var gameTurboManager: GameTurboManager
    @Inject lateinit var profileManager: ProfileManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentPackage: String? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val packageName = intent?.getStringExtra(EXTRA_PACKAGE)
        
        when (action) {
            ACTION_START_BOOST -> {
                packageName?.let { pkg ->
                    currentPackage = pkg
                    startBoost(pkg)
                }
            }
            ACTION_STOP_BOOST -> {
                stopBoost()
            }
        }
        
        return START_STICKY
    }
    
    private fun startBoost(packageName: String) {
        scope.launch {
            val profile = profileManager.loadProfile(packageName).await()
            gameTurboManager.startGameBoost(
                com.mori.downloader.gameturbo.model.GameInfo(
                    packageName = packageName,
                    label = packageName,
                    isGame = true
                )
            ).await()
            
            updateNotification("Boosting $packageName", "Game profile active")
        }
    }
    
    private fun stopBoost() {
        scope.launch {
            gameTurboManager.stopBoost().await()
            stopForeground(true)
            stopSelf()
        }
    }
    
    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GameTurbo::BoostService")
        wakeLock?.acquire()
    }
    
    private fun releaseWakeLock() {
        wakeLock?.release()
        wakeLock = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Turbo Boost",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Game Turbo boost service status"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun updateNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        scope.coroutineContext.cancel()
        releaseWakeLock()
        stopForeground(true)
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}