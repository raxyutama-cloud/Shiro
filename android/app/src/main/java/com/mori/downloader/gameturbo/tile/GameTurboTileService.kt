package com.mori.downloader.gameturbo.tile

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.model.BoostConfig
import com.mori.downloader.gameturbo.core.ProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameTurboTileService : TileService() {
    
    @Inject lateinit var gameTurboManager: GameTurboManager
    @Inject lateinit var profileManager: ProfileManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isBoostActive = false
    
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }
    
    override fun onClick() {
        super.onClick()
        toggleBoost()
    }
    
    private fun toggleBoost() {
        scope.launch {
            if (isBoostActive) {
                gameTurboManager.stopBoost().await()
                isBoostActive = false
            } else {
                val config = profileManager.loadBoostConfig().await()
                gameTurboManager.startBoost(config).await()
                isBoostActive = true
            }
            updateTile()
        }
    }
    
    private fun updateTile() {
        val tile = qsTile ?: return
        
        scope.launch {
            isBoostActive = profileManager.isBoostActive().await()
            
            tile.state = if (isBoostActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = if (isBoostActive) "Game Turbo: ON" else "Game Turbo: OFF"
            tile.subtitle = if (isBoostActive) "Tap to disable" else "Tap to enable"
            tile.icon = Icon.createWithResource(
                applicationContext,
                if (isBoostActive) 
                    com.mori.downloader.R.drawable.ic_gameturbo_active 
                    else com.mori.downloader.R.drawable.ic_gameturbo_inactive
            )
            tile.updateTile()
        }
    }
}