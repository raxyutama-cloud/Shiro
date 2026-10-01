package com.mori.downloader.gameturbo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mori.downloader.gameturbo.model.BoostConfig
import com.mori.downloader.gameturbo.ui.viewmodel.SettingsViewModel
import com.mori.downloader.gameturbo.ui.widget.SettingRow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val shizukuAvailable by viewModel.shizukuAvailable.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
            )
        )
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shizuku Status
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Shizuku Integration", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(
                            if (shizukuAvailable) "Connected ✓" else "Not connected - Tap to setup",
                            fontSize = 14.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!shizukuAvailable) {
                        androidx.compose.material3.Button(onClick = { viewModel.requestShizukuPermission() }) {
                            Text("Setup Shizuku")
                        }
                    }
                }
            }
            
            // Global Boost Config
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("Global Boost Settings", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingRow(
                        title = "CPU Boost",
                        subtitle = "Set CPU governor to performance",
                        value = config.enableCpuBoost,
                        onValueChange = { viewModel.updateConfig(config.copy(enableCpuBoost = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableCpuBoost = it)) })
                    }
                    
                    SettingRow(
                        title = "GPU Boost",
                        subtitle = "Set GPU governor to performance",
                        value = config.enableGpuBoost,
                        onValueChange = { viewModel.updateConfig(config.copy(enableGpuBoost = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableGpuBoost = it)) })
                    }
                    
                    SettingRow(
                        title = "Scheduler Tuning",
                        subtitle = "fpsgo, uclamp, sched_boost",
                        value = config.enableSchedulerTuning,
                        onValueChange = { viewModel.updateConfig(config.copy(enableSchedulerTuning = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableSchedulerTuning = it)) })
                    }
                    
                    SettingRow(
                        title = "Thermal Control",
                        subtitle = "Disable thermal throttling (risky)",
                        value = config.enableThermalControl,
                        onValueChange = { viewModel.updateConfig(config.copy(enableThermalControl = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableThermalControl = it)) })
                    }
                    
                    SettingRow(
                        title = "RAM Cleaner",
                        subtitle = "Kill background apps on boost",
                        value = config.enableRamCleaner,
                        onValueChange = { viewModel.updateConfig(config.copy(enableRamCleaner = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableRamCleaner = it)) })
                    }
                    
                    SettingRow(
                        title = "Network Boost",
                        subtitle = "Set DNS to Google/Cloudflare",
                        value = config.enableNetworkBoost,
                        onValueChange = { viewModel.updateConfig(config.copy(enableNetworkBoost = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableNetworkBoost = it)) })
                    }
                    
                    SettingRow(
                        title = "FPS Monitor",
                        subtitle = "Show real-time FPS overlay",
                        value = config.enableFpsMonitor,
                        onValueChange = { viewModel.updateConfig(config.copy(enableFpsMonitor = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableFpsMonitor = it)) })
                    }
                    
                    SettingRow(
                        title = "Overlay",
                        subtitle = "Floating widget with stats",
                        value = config.enableOverlay,
                        onValueChange = { viewModel.updateConfig(config.copy(enableOverlay = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(enableOverlay = it)) })
                    }
                    
                    SettingRow(
                        title = "Auto Boost on Game Launch",
                        subtitle = "Automatically apply profile when game starts",
                        value = config.autoBoostOnGameLaunch,
                        onValueChange = { viewModel.updateConfig(config.copy(autoBoostOnGameLaunch = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(autoBoostOnGameLaunch = it)) })
                    }
                    
                    SettingRow(
                        title = "Aggressive Mode",
                        subtitle = "Maximum performance, higher battery drain",
                        value = config.aggressiveMode,
                        onValueChange = { viewModel.updateConfig(config.copy(aggressiveMode = it)) }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { viewModel.updateConfig(config.copy(aggressiveMode = it)) })
                    }
                }
            }
            
            // Overlay Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("Overlay Settings", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingRow(
                        title = "Overlay Position",
                        subtitle = "Top-left, Top-right, Bottom-left, Bottom-right",
                        value = "top-right",
                        onValueChange = { /* TODO */ }
                    ) { value ->
                        androidx.compose.material3.TextField(value = value, onValueChange = { /* TODO */ }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    
                    SettingRow(
                        title = "Overlay Transparency",
                        value = "80%",
                        onValueChange = { /* TODO */ }
                    ) { value ->
                        androidx.compose.material3.Slider(value = 0.8f, onValueChange = { /* TODO */ }, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
                    }
                    
                    SettingRow(
                        title = "Show FPS",
                        value = true,
                        onValueChange = { /* TODO */ }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { /* TODO */ })
                    }
                    
                    SettingRow(
                        title = "Show Temperature",
                        value = true,
                        onValueChange = { /* TODO */ }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { /* TODO */ })
                    }
                    
                    SettingRow(
                        title = "Show CPU/GPU Usage",
                        value = true,
                        onValueChange = { /* TODO */ }
                    ) { value ->
                        Switch(checked = value, onCheckedChange = { /* TODO */ })
                    }
                }
            }
            
            // Advanced
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("Advanced", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingRow(
                        title = "Reset All Profiles",
                        subtitle = "Delete all per-game profiles",
                        value = false,
                        onValueChange = { viewModel.resetAllProfiles() }
                    ) { value ->
                        androidx.compose.material3.Button(onClick = { viewModel.resetAllProfiles() }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer)) {
                            Text("Reset Profiles")
                        }
                    }
                    
                    SettingRow(
                        title = "Export Configuration",
                        subtitle = "Backup profiles and settings",
                        value = false,
                        onValueChange = { viewModel.exportConfig() }
                    ) { value ->
                        androidx.compose.material3.Button(onClick = { viewModel.exportConfig() }) {
                            Text("Export")
                        }
                    }
                    
                    SettingRow(
                        title = "Import Configuration",
                        subtitle = "Restore from backup",
                        value = false,
                        onValueChange = { viewModel.importConfig() }
                    ) { value ->
                        androidx.compose.material3.Button(onClick = { viewModel.importConfig() }) {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }
}