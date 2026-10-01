package com.mori.downloader.gameturbo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mori.downloader.gameturbo.core.GameDetector
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.model.BoostConfig
import com.mori.downloader.gameturbo.model.GameInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class GameTurboViewModel @Inject constructor(
    application: Application,
    private val gameTurboManager: GameTurboManager,
    private val gameDetector: GameDetector,
    private val profileManager: com.mori.downloader.gameturbo.core.ProfileManager
) : AndroidViewModel(application) {
    
    // State
    private val _games = MutableStateFlow<List<GameInfo>>(emptyList())
    val games: StateFlow<List<GameInfo>> = _games
    
    private val _boostState = MutableStateFlow<BoostState>(BoostState.Idle)
    val boostState: StateFlow<BoostState> = _boostState
    
    private val _config = MutableStateFlow<BoostConfig>(BoostConfig())
    val config: StateFlow<BoostConfig> = _config
    
    sealed interface BoostState {
        data class Boosting(val profile: com.mori.downloader.gameturbo.model.GameProfile) : BoostState
        data class GameProfileActive(val game: GameInfo, val profile: com.mori.downloader.gameturbo.model.GameProfile) : BoostState
        object Idle : BoostState
        data class Error(val message: String) : BoostState
    }
    
    init {
        loadGames()
        loadConfig()
        observeBoostState()
    }
    
    private fun loadGames() {
        viewModelScope.launch {
            _games.value = gameDetector.getAllGames().await()
        }
    }
    
    private fun loadConfig() {
        viewModelScope.launch {
            _config.value = profileManager.loadBoostConfig().await()
        }
    }
    
    private fun observeBoostState() {
        viewModelScope.launch {
            gameTurboManager.boostState.collect { state ->
                _boostState.value = when (state) {
                    is GameTurboManager.BoostState.Boosting -> BoostState.Boosting(state.profile)
                    is GameTurboManager.BoostState.GameProfileActive -> BoostState.GameProfileActive(state.game, state.profile)
                    is GameTurboManager.BoostState.Idle -> BoostState.Idle
                    is GameTurboManager.BoostState.Error -> BoostState.Error(state.message)
                }
            }
        }
    }
    
    fun startGlobalBoost() {
        viewModelScope.launch {
            val config = _config.value
            gameTurboManager.startBoost(config).await()
        }
    }
    
    fun startGameBoost(game: GameInfo) {
        viewModelScope.launch {
            gameTurboManager.startGameBoost(game).await()
        }
    }
    
    fun stopBoost() {
        viewModelScope.launch {
            gameTurboManager.stopBoost().await()
        }
    }
    
    fun refreshGames() {
        viewModelScope.launch {
            _games.value = gameDetector.getAllGames(true).await()
        }
    }
    
    fun onGameLaunched(packageName: String) {
        gameTurboManager.onGameLaunched(packageName)
    }
    
    fun onGameExited(packageName: String) {
        gameTurboManager.onGameExited(packageName)
    }
}