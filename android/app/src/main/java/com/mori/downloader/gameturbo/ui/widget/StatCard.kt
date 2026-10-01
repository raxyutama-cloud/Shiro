package com.mori.downloader.gameturbo.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mori.downloader.gameturbo.model.CPUStats
import com.mori.downloader.gameturbo.model.GPUStats
import com.mori.downloader.gameturbo.model.MemoryStats
import com.mori.downloader.gameturbo.model.ThermalInfo

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    trend: Float = 0f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icon?.let {
                        Icon(imageVector = it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Text(text = title, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                }
                if (trend != 0f) {
                    Icon(
                        imageVector = if (trend > 0) 
                            androidx.compose.material.icons.Icons.Default.TrendingUp 
                            else androidx.compose.material.icons.Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (trend > 0) Color.Green else Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = color
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CPUSummaryCard(stats: CPUStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Memory,
                        contentDescription = null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("CPU", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Text(
                    text = "${stats.governor} | ${stats.currentFreqMHz} MHz",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Usage bar
            UsageBar(
                progress = stats.usagePercent / 100f,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                label = "${stats.usagePercent}% usage"
            )
            
            // Core frequencies
            if (stats.loadPerCore.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.loadPerCore.forEachIndexed { index, freq ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                                .height(60.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val barHeight = size.height * (freq / 3000f).coerceIn(0f, 1f)
                                drawRect(
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    topLeft = androidx.compose.ui.geometry.Offset(4.dp.toPx(), size.height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(size.width - 8.dp.toPx(), barHeight)
                                )
                            }
                            Text(
                                text = "C$index",
                                fontSize = 10.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GPUSummaryCard(stats: GPUStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("GPU", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Text(
                    text = "${stats.governor} | ${stats.currentFreqMHz} MHz",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            UsageBar(
                progress = stats.usagePercent / 100f,
                color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                label = "${stats.usagePercent}% usage | ${stats.temperature}°C"
            )
        }
    }
}

@Composable
fun MemorySummaryCard(stats: MemoryStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SdStorage,
                        contentDescription = null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Memory", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Text(
                    text = "${formatBytes(stats.usedRam)} / ${formatBytes(stats.totalRam)}",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            UsageBar(
                progress = stats.usedRam.toFloat() / stats.totalRam.toFloat(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                label = "${(stats.usedRam.toFloat() / stats.totalRam.toFloat() * 100).toInt()}% used"
            )
        }
    }
}

@Composable
fun ThermalSummaryCard(stats: ThermalInfo) {
    val (color, label) = when (stats.throttlingLevel) {
        0 -> androidx.compose.material3.MaterialTheme.colorScheme.primary to "Normal"
        1 -> Color.Yellow to "Warm"
        2 -> Color.Orange to "Hot"
        3 -> Color.Red to "Throttling"
        4 -> Color.Magenta to "Critical"
        else -> androidx.compose.material3.MaterialTheme.colorScheme.primary to "Unknown"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Thermal", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Text(
                    text = "${stats.currentTemp}°C • $label",
                    fontSize = 14.sp,
                    color = color
                )
            }
            
            UsageBar(
                progress = (stats.currentTemp / 100f).coerceIn(0f, 1f),
                color = color,
                label = "Throttling Level: ${stats.throttlingLevel}/4"
            )
        }
    }
}

@Composable
fun UsageBar(progress: Float, color: Color, label: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            val width = size.width
            val barWidth = width * progress.coerceIn(0f, 1f)
            
            // Background
            drawRect(
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                size = size
            )
            
            // Progress
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, size.height)
            )
        }
        Text(text = label, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
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