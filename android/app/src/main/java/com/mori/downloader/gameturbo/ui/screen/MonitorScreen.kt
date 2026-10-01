package com.mori.downloader.gameturbo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mori.downloader.gameturbo.model.CPUStats
import com.mori.downloader.gameturbo.model.GPUStats
import com.mori.downloader.gameturbo.model.MemoryStats
import com.mori.downloader.gameturbo.model.ThermalInfo
import com.mori.downloader.gameturbo.ui.viewmodel.MonitorViewModel
import com.mori.downloader.gameturbo.ui.widget.StatCard
import com.mori.downloader.gameturbo.ui.widget.UsageChart

@Composable
fun MonitorScreen(viewModel: MonitorViewModel) {
    val cpuStats by viewModel.cpuStats.collectAsStateWithLifecycle()
    val gpuStats by viewModel.gpuStats.collectAsStateWithLifecycle()
    val memoryStats by viewModel.memoryStats.collectAsStateWithLifecycle()
    val thermalStats by viewModel.thermalStats.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Real-time Monitor", fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
            )
        )
        
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FPS Counter (Top)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f".format(fps),
                            fontSize = 64.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "FPS",
                            fontSize = 16.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Stats Grid
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.foundation.lazy.items(listOf(
                    "CPU" to cpuStats,
                    "GPU" to gpuStats,
                    "Memory" to memoryStats,
                    "Thermal" to thermalStats
                )) { (title, stats) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                when (stats) {
                                    is CPUStats -> Text(text = "${stats.usagePercent}% | ${stats.currentFreqMHz}MHz", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                    is GPUStats -> Text(text = "${stats.currentFreqMHz}MHz | ${stats.governor}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                    is MemoryStats -> Text(text = "Used: ${formatBytes(stats.usedRam)} / ${formatBytes(stats.totalRam)}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                    is ThermalInfo -> Text(text = "${stats.currentTemp}°C | Level ${stats.throttlingLevel}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            // Mini chart
                            UsageChart(data = when (stats) {
                                is CPUStats -> stats.loadPerCore.map { it / 100f }
                                is GPUStats -> listOf(stats.usagePercent / 100f)
                                is MemoryStats -> listOf(stats.usedRam.toFloat() / stats.totalRam.toFloat())
                                is ThermalInfo -> listOf(stats.currentTemp / 100f)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageChart(data: List<Float>) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height
        val step = width / data.size.max(1)
        
        data.forEachIndexed { index, value ->
            val x = index * step
            val barHeight = height * value.coerceIn(0f, 1f)
            drawRect(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(x + 2, height - barHeight),
                size = androidx.compose.ui.geometry.Size(step - 4, barHeight)
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}