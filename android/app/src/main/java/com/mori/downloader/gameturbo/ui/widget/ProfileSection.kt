package com.mori.downloader.gameturbo.ui.widget

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileSection(
    title: String,
    icon: String = "",
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon.isNotBlank()) {
                    Icon(
                        imageVector = getIcon(icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 8.dp))
                }
                Text(text = title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            androidx.compose.material3.Divider()
            content()
        }
    }
}

@Composable
fun SettingRow<T>(
    title: String,
    subtitle: String = "",
    value: T,
    onValueChange: (T) -> Unit,
    control: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 16.sp)
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            control(value)
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    control: @Composable (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 16.sp)
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            control(value)
        }
    }
}

private fun getIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "cpu" -> androidx.compose.material.icons.Icons.Default.Memory
        "gpu" -> androidx.compose.material.icons.Icons.Default.DeveloperBoard
        "tune" -> androidx.compose.material.icons.Icons.Default.Tune
        "display" -> androidx.compose.material.icons.Icons.Default.DisplaySettings
        "graphics" -> androidx.compose.material.icons.Icons.Default.Games
        else -> androidx.compose.material.icons.Icons.Default.Settings
    }
}