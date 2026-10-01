package com.mori.downloader.gameturbo.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mori.downloader.gameturbo.core.ProfileManager
import com.mori.downloader.gameturbo.core.ShizukuHelper
import com.mori.downloader.gameturbo.model.BoostConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val profileManager: ProfileManager,
    private val shizukuHelper: ShizukuHelper
) : AndroidViewModel(application) {
    
    private val _config = MutableStateFlow<BoostConfig>(BoostConfig())
    val config: StateFlow<BoostConfig> = _config
    
    private val _shizukuAvailable = MutableStateFlow<Boolean>(false)
    val shizukuAvailable: StateFlow<Boolean> = _shizukuAvailable
    
    init {
        loadConfig()
        observeShizuku()
    }
    
    private fun loadConfig() {
        viewModelScope.launch {
            _config.value = profileManager.loadBoostConfig().await()
        }
    }
    
    private fun observeShizuku() {
        viewModelScope.launch {
            shizukuHelper.isAvailable.collect { available ->
                _shizukuAvailable.value = available
            }
        }
    }
    
    fun updateConfig(newConfig: BoostConfig) {
        _config.value = newConfig
        viewModelScope.launch {
            profileManager.saveBoostConfig(newConfig).await()
        }
    }
    
    fun requestShizukuPermission() {
        viewModelScope.launch {
            val granted = shizukuHelper.requestPermission()
            if (granted) {
                _shizukuAvailable.value = true
            }
        }
    }
    
    fun resetAllProfiles() {
        viewModelScope.launch {
            profileManager.loadAllProfiles().await().keys.forEach { pkg ->
                profileManager.deleteProfile(pkg).await()
            }
        }
    }
    
    fun exportConfig() {
        viewModelScope.launch {
            val profiles = profileManager.loadAllProfiles().await()
            val config = _config.value
            
            val backup = GameTurboBackup(
                version = 1,
                timestamp = System.currentTimeMillis(),
                boostConfig = config,
                profiles = profiles
            )
            
            val json = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(backup)
            
            // Save to Downloads
            val resolver = getApplication<Application>().contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "gameturbo_backup_${System.currentTimeMillis()}.json")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/Shiro")
            }
            
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { uri ->
                resolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray())
                }
            }
        }
    }
    
    fun importConfig() {
        // TODO: Implement file picker
    }
}

import kotlinx.serialization.Serializable

@Serializable
data class GameTurboBackup(
    val version: Int,
    val timestamp: Long,
    val boostConfig: BoostConfig,
    val profiles: Map<String, com.mori.downloader.gameturbo.model.GameProfile>
)