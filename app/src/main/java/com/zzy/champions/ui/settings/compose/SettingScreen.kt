package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.zzy.champions.R
import com.zzy.champions.ui.settings.SettingsViewModel

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    var showRefreshDialog by remember { mutableStateOf(false) }

    if (showRefreshDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            title = { Text(stringResource(R.string.refresh_data_dialog_title)) },
            text = { Text(stringResource(R.string.refresh_data_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRefreshDialog = false
                    viewModel.refreshData(onDone = onRefreshDone)
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRefreshDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { SettingAppbar(onBack = onBack) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SettingItem(itemName = stringResource(R.string.switch_language)) {
                IconButton(onClick = onLanguageClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
            SettingItem(itemName = stringResource(R.string.refresh_data)) {
                IconButton(onClick = { showRefreshDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
