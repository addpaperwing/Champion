package com.zzy.champions.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onItemClick: (ChampionBuild) -> Unit,
    onInsertItem: (Int, String, String) -> Unit,
    onDeleteItem: (ChampionBuild) -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        builds.forEach {
            ChampionBuildItem(cb = it, onClick = onItemClick, onEditClick = onInsertItem, onDeleteClick = onDeleteItem)
        }
    }
}

@Composable
fun ChampionBuildItem(
    modifier: Modifier = Modifier,
    cb: ChampionBuild,
    onClick: (ChampionBuild) -> Unit,
    onEditClick: (Int, String, String) -> Unit,
    onDeleteClick: (ChampionBuild) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }

    Column(modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(cb) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                Row {
                    Text(
                        text = cb.nameOfBuild,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = {
                        showMenu = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "menu",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

//                Surface(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(CardDefaults.shape),
//                    color = Color.Transparent,
//                ) {}
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }

    if (showMenu) {
        MenuDialog(
            onDismissRequest = { showMenu = false },
            onEdit = {
                showMenu = false
                showEditor = true
            },
            onDelete = {
                showMenu = false
                onDeleteClick(cb)
            }
        )
    }

    if (showEditor) {
        ChampionBuildDialog(
            onDismissRequest = { showEditor = false },
            initTitle = cb.nameOfBuild,
            initContent = cb.url,
            onOkClick = { title, content ->
                showEditor = false
                onEditClick(cb.id, title, content)
            }
        )
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
        ChampionBuildScreen(builds = list, onItemClick = {

        }, onInsertItem = { a, b, c ->

        }, onDeleteItem = {

        })
    }
}