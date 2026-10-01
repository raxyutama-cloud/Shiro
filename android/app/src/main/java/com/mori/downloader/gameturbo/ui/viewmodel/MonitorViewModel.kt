package com.mori.downloader.gameturbo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.core.ShizukuHelper
import com.mori.downloader.gameturbo.model.CPUStats
import com.mori.downloader.gameturbo.model.GPUStats
import com.mori.downloader.gameturbo.model.MemoryStats
import com.mori.downloader.gameturbo.model.ThermalInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MonitorViewModel @Inject constructor(
    application: Application,
    private val gameTurboManager: GameTurboManager
) : AndroidViewModel(application) {
    
    private val _cpuStats = MutableStateFlow<CPUStats>(CPUStats())
    val cpuStats: StateFlow<CPUStats> = _cpuStats
    
    private val _gpuStats = MutableStateFlow<GPUStats>(GPUStats())
    val gpuStats: StateFlow<GPUStats> = _gpuStats
    
    private val _memoryStats = MutableStateFlow<MemoryStats>(MemoryStats())
    val memoryStats: StateFlow<MemoryStats> = _memoryStats
    
    private val _thermalStats = MutableStateFlow<ThermalInfo>(ThermalInfo())
    val thermalStats: StateFlow<ThermalInfo> = _thermalStats
    
    private val _fps = MutableStateFlow<Float>(60f)
    val fps: StateFlow<Float> = _fps
    
    init {
        observeStats()
    }
    
    private fun observeStats() {
        viewModelScope.launch {
            gameTurboManager.stats.collect { stats ->
                when (stats) {
                    is GameTurboManager.GameStats.Live -> {
                        _cpuStats.value = stats.cpu
                        _gpuStats.value = stats.gpu
                        _memoryStats.value = stats.memory
                        _thermalStats.value = stats.thermal
                        _fps.value = stats.fps
                    }
                    is GameTurboManager.GameStats.Empty -> {
                        _cpuStats.value = CPUStats()
                        _gpuStats.value = GPUStats()
                        _memoryStats.value = MemoryStats()
                        _thermalStats.value = ThermalInfo()
                        _fps.value = 0f
                    }
                }
            }
        }
    }
}