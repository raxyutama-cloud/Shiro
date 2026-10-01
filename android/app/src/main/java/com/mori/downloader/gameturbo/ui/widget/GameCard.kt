package com.mori.downloader.gameturbo.ui.widget

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PorterDuffColorFilter
import androidx.compose.ui.graphics.PorterDuffMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mori.downloader.gameturbo.model.GameInfo

@Composable
fun GameCard(
    game: GameInfo,
    isBoosted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isBoosted) 
                androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer 
                else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Icon
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (game.icon != null) {
                    androidx.compose.ui.graphics.painter.Painter(
                        androidx.compose.ui.graphics.painter.Painter.Companion
                    )
                    // Use Android's drawable
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.painter.Painter.Companion,
                        contentDescription = game.label,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SportsEsports,
                        contentDescription = game.label,
                        modifier = Modifier.size(48.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Boost indicator
                if (isBoosted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.shape.CircleShape
                        androidx.compose.foundation.background(
                            modifier = Modifier.size(20.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.FlashOn,
                            contentDescription = "Boosted",
                            modifier = Modifier.size(14.dp),
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            // Game Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.label,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                    color = if (isBoosted) 
                        androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer 
                        else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = game.packageName,
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (game.category.isNotBlank()) {
                        Chip(text = game.category)
                    }
                    Text(
                        text = "v${game.versionName}",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Chevron
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun Chip(text: String) {
    androidx.compose.material3.Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
    androidx.compose.foundation.background(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
    )
    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}