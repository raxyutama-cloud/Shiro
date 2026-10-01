package com.mori.downloader.gameturbo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mori.downloader.gameturbo.model.GameProfile
import com.mori.downloader.gameturbo.ui.viewmodel.ProfileViewModel
import com.mori.downloader.gameturbo.ui.widget.ProfileSection
import com.mori.downloader.gameturbo.ui.widget.SettingRow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    packageName: String,
    viewModel: ProfileViewModel
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Game Profile", fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
            ),
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { viewModel.onBack() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CPU Section
            ProfileSection(title = "CPU Control", icon = "cpu") {
                SettingRow(
                    title = "CPU Governor",
                    subtitle = "performance, powersave, schedutil, ondemand",
                    value = profile.cpuGovernor,
                    onValueChange = { viewModel.updateProfile(profile.copy(cpuGovernor = it)) }
                ) { value ->
                    androidx.compose.material3.TextField(
                        value = value,
                        onValueChange = { viewModel.updateProfile(profile.copy(cpuGovernor = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingRow(
                    title = "Min Frequency (MHz)",
                    subtitle = "0 = Auto",
                    value = "${profile.cpuMinFreq}",
                    onValueChange = { viewModel.updateProfile(profile.copy(cpuMinFreq = it.toIntOrNull() ?: 0)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 0f,
                        onValueChange = { viewModel.updateProfile(profile.copy(cpuMinFreq = it.toInt())) },
                        valueRange = 0f..3000f,
                        steps = 30,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Max Frequency (MHz)",
                    subtitle = "0 = Max",
                    value = "${profile.cpuMaxFreq}",
                    onValueChange = { viewModel.updateProfile(profile.copy(cpuMaxFreq = it.toIntOrNull() ?: 0)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 0f,
                        onValueChange = { viewModel.updateProfile(profile.copy(cpuMaxFreq = it.toInt())) },
                        valueRange = 0f..3500f,
                        steps = 35,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // GPU Section
            ProfileSection(title = "GPU Control", icon = "gpu") {
                SettingRow(
                    title = "GPU Governor",
                    subtitle = "performance, simple_ondemand, msm-adreno-tz",
                    value = profile.gpuGovernor,
                    onValueChange = { viewModel.updateProfile(profile.copy(gpuGovernor = it)) }
                ) { value ->
                    androidx.compose.material3.TextField(
                        value = value,
                        onValueChange = { viewModel.updateProfile(profile.copy(gpuGovernor = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingRow(
                    title = "Min Frequency (MHz)",
                    value = "${profile.gpuMinFreq}",
                    onValueChange = { viewModel.updateProfile(profile.copy(gpuMinFreq = it.toIntOrNull() ?: 0)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 0f,
                        onValueChange = { viewModel.updateProfile(profile.copy(gpuMinFreq = it.toInt())) },
                        valueRange = 0f..1000f,
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Max Frequency (MHz)",
                    value = "${profile.gpuMaxFreq}",
                    onValueChange = { viewModel.updateProfile(profile.copy(gpuMaxFreq = it.toIntOrNull() ?: 0)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 0f,
                        onValueChange = { viewModel.updateProfile(profile.copy(gpuMaxFreq = it.toInt())) },
                        valueRange = 0f..1000f,
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // System Tuning
            ProfileSection(title = "System Tuning", icon = "tune") {
                SettingRow(
                    title = "Scheduler Tuning",
                    subtitle = "fpsgo, uclamp, sched_boost",
                    value = profile.schedulerTuning,
                    onValueChange = { viewModel.updateProfile(profile.copy(schedulerTuning = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(schedulerTuning = it)) })
                }
                
                SettingRow(
                    title = "Disable Thermal Throttling",
                    subtitle = "Risky: may cause overheating",
                    value = profile.disableThermalThrottling,
                    onValueChange = { viewModel.updateProfile(profile.copy(disableThermalThrottling = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(disableThermalThrottling = it)) })
                }
                
                SettingRow(
                    title = "Clear RAM Before Launch",
                    value = profile.clearRamBeforeLaunch,
                    onValueChange = { viewModel.updateProfile(profile.copy(clearRamBeforeLaunch = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(clearRamBeforeLaunch = it)) })
                }
                
                SettingRow(
                    title = "Block Background Apps",
                    value = profile.blockBackgroundApps,
                    onValueChange = { viewModel.updateProfile(profile.copy(blockBackgroundApps = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(blockBackgroundApps = it)) })
                }
                
                SettingRow(
                    title = "Network Priority",
                    value = profile.networkPriority,
                    onValueChange = { viewModel.updateProfile(profile.copy(networkPriority = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(networkPriority = it)) })
                }
            }
            
            // Display & Audio
            ProfileSection(title = "Display & Audio", icon = "display") {
                SettingRow(
                    title = "Screen Off Timeout (ms)",
                    subtitle = "0 = System default",
                    value = "${profile.screenOffTimeout}",
                    onValueChange = { viewModel.updateProfile(profile.copy(screenOffTimeout = it.toIntOrNull() ?: 0)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 0f,
                        onValueChange = { viewModel.updateProfile(profile.copy(screenOffTimeout = it.toInt())) },
                        valueRange = 0f..1800000f,
                        steps = 36,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Brightness",
                    subtitle = "-1 = System default",
                    value = "${profile.brightness}",
                    onValueChange = { viewModel.updateProfile(profile.copy(brightness = it.toIntOrNull() ?: -1)) }
                ) { value ->
                    Slider(
                        value = (value.toFloatOrNull() ?: -1f + 1f),
                        onValueChange = { viewModel.updateProfile(profile.copy(brightness = (it - 1).toInt())) },
                        valueRange = 0f..255f,
                        steps = 25,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Volume Boost",
                    value = "%.0f%%".format(profile.volumeBoost * 100),
                    onValueChange = { viewModel.updateProfile(profile.copy(volumeBoost = (it.toFloatOrNull() ?: 100f) / 100f)) }
                ) { value ->
                    Slider(
                        value = (value.toFloatOrNull() ?: 1f) * 100,
                        onValueChange = { viewModel.updateProfile(profile.copy(volumeBoost = it / 100f)) },
                        valueRange = 50f..200f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Graphics
            ProfileSection(title = "Graphics", icon = "graphics") {
                SettingRow(
                    title = "FPS Target",
                    value = "${profile.fpsTarget}",
                    onValueChange = { viewModel.updateProfile(profile.copy(fpsTarget = it.toIntOrNull() ?: 60)) }
                ) { value ->
                    Slider(
                        value = value.toFloatOrNull() ?: 60f,
                        onValueChange = { viewModel.updateProfile(profile.copy(fpsTarget = it.toInt())) },
                        valueRange = 30f..144f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Resolution Scale",
                    value = "%.0f%%".format(profile.resolutionScale * 100),
                    onValueChange = { viewModel.updateProfile(profile.copy(resolutionScale = (it.toFloatOrNull() ?: 100f) / 100f)) }
                ) { value ->
                    Slider(
                        value = (value.toFloatOrNull() ?: 1f) * 100,
                        onValueChange = { viewModel.updateProfile(profile.copy(resolutionScale = it / 100f)) },
                        valueRange = 50f..100f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                SettingRow(
                    title = "Texture Quality",
                    value = profile.textureQuality,
                    onValueChange = { viewModel.updateProfile(profile.copy(textureQuality = it)) }
                ) { value ->
                    androidx.compose.material3.TextField(
                        value = value,
                        onValueChange = { viewModel.updateProfile(profile.copy(textureQuality = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                SettingRow(
                    title = "Shadows",
                    value = profile.shadowsEnabled,
                    onValueChange = { viewModel.updateProfile(profile.copy(shadowsEnabled = it)) }
                ) { value ->
                    Switch(checked = value, onCheckedChange = { viewModel.updateProfile(profile.copy(shadowsEnabled = it)) })
                }
                
                SettingRow(
                    title = "Anti-Aliasing",
                    value = profile.antiAliasing,
                    onValueChange = { viewModel.updateProfile(profile.copy(antiAliasing = it)) }
                ) { value ->
                    androidx.compose.material3.TextField(
                        value = value,
                        onValueChange = { viewModel.updateProfile(profile.copy(antiAliasing = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}