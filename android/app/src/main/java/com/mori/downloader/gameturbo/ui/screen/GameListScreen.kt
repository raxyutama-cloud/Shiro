package com.mori.downloader.gameturbo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavController
import com.mori.downloader.gameturbo.model.GameInfo
import com.mori.downloader.gameturbo.ui.viewmodel.GameTurboViewModel
import com.mori.downloader.gameturbo.ui.widget.GameCard
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GameListScreen(
    navController: NavController,
    onGameClick: (GameInfo) -> Unit,
    viewModel: GameTurboViewModel
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val boostState by viewModel.boostState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Game Turbo", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                )
            )
            
            // Boost status banner
            when (boostState) {
                is GameTurboViewModel.BoostState.Boosting -> {
                    BoostStatusBanner(message = "Global Boost Active", onDismiss = { viewModel.stopBoost() })
                }
                is GameTurboViewModel.BoostState.GameProfileActive -> {
                    BoostStatusBanner(message = "Boosting: ${boostState.game.label}", onDismiss = { viewModel.stopBoost() })
                }
                else -> {}
            }
            
            // Game list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games) { game ->
                    GameCard(
                        game = game,
                        isBoosted = boostState is GameTurboViewModel.BoostState.GameProfileActive && boostState.game.packageName == game.packageName,
                        onClick = { onGameClick(game) }
                    )
                }
            }
        }
    }
}

@Composable
fun BoostStatusBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer)
            androidx.compose.material3.IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}