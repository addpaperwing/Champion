package com.zzy.champions.ui.detail.compose.build

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.compose.ChampionBuildDialog
import com.zzy.champions.ui.compose.MenuDialog
import com.zzy.champions.ui.compose.TextDialog

@Composable
fun BuildItem(
    modifier: Modifier = Modifier,
    build: ChampionBuild,
    onClick: (ChampionBuild) -> Unit,
    onEditClick: (ChampionBuild) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }

    Column(modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape)
                .clickable { onClick(build) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = build.nameOfBuild,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f),
                        maxLines = 1
                    )
                    IconButton(
                        onClick = {
                        showMenu = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Build item menu",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
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
                showConfirmation = true
            }
        )
    }

    if (showConfirmation) {
        TextDialog(
            onDismissRequest = { showConfirmation = false },
            onPositiveButtonClick = {
                onDeleteClick(build.id)
            },
            content = {
                Text(text = stringResource(id = R.string.do_you_want_to_delete_this_build))
            })
    }

    if (showEditor) {
        ChampionBuildDialog(
            onDismissRequest = { showEditor = false },
            name = build.nameOfBuild,
            desc = build.url,
            onOkClick = { name, url ->
                showEditor = false
                build.nameOfBuild = name
                build.url = url
                onEditClick(build)
            }
        )
    }
}