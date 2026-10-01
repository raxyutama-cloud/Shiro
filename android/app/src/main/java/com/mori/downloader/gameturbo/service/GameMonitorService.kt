package com.mori.downloader.gameturbo.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.mori.downloader.gameturbo.core.GameDetector
import com.mori.downloader.gameturbo.core.GameTurboManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameMonitorService : Service() {
    
    @Inject lateinit var gameTurboManager: GameTurboManager
    @Inject lateinit var gameDetector: GameDetector
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastRunningGames = mutableSetOf<String>()
    
    override fun onCreate() {
        super.onCreate()
        startMonitoring()
    }
    
    private fun startMonitoring() {
        scope.launch {
            while (true) {
                checkRunningGames()
                delay(2000) // Check every 2 seconds
            }
        }
    }
    
    private fun checkRunningGames() {
        scope.launch {
            // Get all known games
            val games = gameDetector.getAllGames().await()
            val currentRunning = mutableSetOf<String>()
            
            games.forEach { game ->
                if (gameDetector.isGameRunning(game.packageName).await()) {
                    currentRunning.add(game.packageName)
                }
            }
            
            // Detect new games launched
            val newlyLaunched = currentRunning - lastRunningGames
            newlyLaunched.forEach { pkg ->
                gameTurboManager.onGameLaunched(pkg)
            }
            
            // Detect games exited
            val exited = lastRunningGames - currentRunning
            exited.forEach { pkg ->
                gameTurboManager.onGameExited(pkg)
            }
            
            lastRunningGames = currentRunning
        }
    }
    
    override fun onDestroy() {
        scope.coroutineContext.cancel()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}