package com.zzy.champions.ui.grid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.DetailActivity
import com.zzy.champions.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ChampionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var showLandingScreen by rememberSaveable { mutableStateOf(true) }
                if (showLandingScreen) {
                    LaunchScreen(modifier = Modifier, onTimeout = { showLandingScreen = false })
                } else {
                    Scaffold(
                        topBar = {
                            TopBar()
                        }
                    ) { padding ->
                        ChampionIndexScreen(
                            modifier = Modifier.padding(padding),
                            viewModel = viewModel
                        ) {
                            startActivity(Intent(this, DetailActivity::class.java))
                        }
                    }
                }
            }
        }

        viewModel.getAllChampions()
    }
}

@Composable
fun ChampionIndexScreen(modifier: Modifier = Modifier, viewModel: ChampionsViewModel, onItemClick: (Champion) -> Unit) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    if (champions is UiState.Success) {
        ChampionsGrid(modifier = modifier, champions = (champions as UiState.Success).data, onItemClick)
    }
}