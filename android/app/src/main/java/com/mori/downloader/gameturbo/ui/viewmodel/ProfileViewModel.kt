package com.mori.downloader.gameturbo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mori.downloader.gameturbo.core.ProfileManager
import com.mori.downloader.gameturbo.model.GameProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val profileManager: ProfileManager
) : AndroidViewModel(application) {
    
    private val _profile = MutableStateFlow<GameProfile>(GameProfile(packageName = ""))
    val profile: StateFlow<GameProfile> = _profile
    
    var packageName: String = ""
        private set
    
    init {
        loadProfile()
    }
    
    fun setPackageName(pkg: String) {
        packageName = pkg
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = profileManager.loadProfile(packageName).await()
        }
    }
    
    fun updateProfile(newProfile: GameProfile) {
        _profile.value = newProfile
        viewModelScope.launch {
            profileManager.saveProfile(newProfile).await()
        }
    }
    
    fun onBack() {
        // Navigation handled by caller
    }
}