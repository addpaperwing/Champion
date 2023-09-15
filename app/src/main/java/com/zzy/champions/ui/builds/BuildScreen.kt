package com.zzy.champions.ui.builds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun ChampionBuildScreen(
    modifier: Modifier = Modifier,
    builds: List<ChampionBuild>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        builds.forEach {
            ChampionBuildItem(cb = it, onClick = onItemClick)
        }
    }
}

@Composable
fun ChampionBuildItem(modifier: Modifier = Modifier, cb: ChampionBuild, onClick: (String) -> Unit) {
    Column(modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick(cb.url)
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = cb.name,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }
}

@Composable
@Preview
fun PreviewBuilds() {
    val list = listOf(
        ChampionBuild(1, "OP.GG", "123123"),
        ChampionBuild(2, "U.GG", "123123"),
        ChampionBuild(3, "WP.GG", "123123")
    )
    MyApplicationTheme {
        ChampionBuildScreen(builds = list) {

        }
    }
}