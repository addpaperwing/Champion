package com.zzy.champions.ui.detail.compose

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.compose.ChampionBuildDialog

@Composable
fun AddChampionBuildFAB(onInsertBuild: (ChampionBuild) -> Unit) {
    var showNewBuildEditor by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showNewBuildEditor = true },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
    }

    if (showNewBuildEditor) {
        ChampionBuildDialog(
            onDismissRequest = { showNewBuildEditor = false },
            build = null,
            onOkClick = { cb ->
                showNewBuildEditor = false
                onInsertBuild(cb)
            }
        )
    }
}