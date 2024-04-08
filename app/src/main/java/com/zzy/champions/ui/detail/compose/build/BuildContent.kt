package com.zzy.champions.ui.detail.compose.build

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun ChampionBuildList(
    modifier: Modifier = Modifier,
    builds: List<ChampionBuild>,
    onItemClick: (ChampionBuild) -> Unit,
    onEditBuild: (ChampionBuild) -> Unit,
    onDeleteItem: (Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .semantics { contentDescription = "Champion build" },
    ) {
        items(builds) {
            BuildItem(
                build = it,
                onClick = onItemClick,
                onEditClick = onEditBuild,
                onDeleteClick = onDeleteItem
            )
        }
    }
}

@Composable
@Preview
fun PreviewBuilds() {
    val list = listOf(
        ChampionBuild( "OP.GG", "123123"),
        ChampionBuild( "U.GG", "123123"),
        ChampionBuild( "WP.GG", "123123")
    )
    MyApplicationTheme {
        ChampionBuildList(builds = list, onItemClick = {

        }, onEditBuild = {

        }, onDeleteItem = {

        })
    }
}