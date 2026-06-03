package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.zzy.champions.R
import com.zzy.champions.ui.compose.TextDialog
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
        TextDialog(
            onDismissRequest = { showRefreshDialog = false },
            title = stringResource(R.string.refresh_data_dialog_title),
            positiveButtonText = stringResource(R.string.confirm),
            negativeButtonText = stringResource(R.string.cancel),
            onPositiveButtonClick = {
                showRefreshDialog = false
                viewModel.refreshData(onDone = onRefreshDone)
            },
            onNegativeButtonClick = { showRefreshDialog = false }
        ) {
            Text(
                text = stringResource(R.string.refresh_data_dialog_message),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { SettingAppbar(onBack = onBack) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SettingItem(
                itemName = stringResource(R.string.switch_language),
                description = stringResource(R.string.switch_language_desc)
            ) {
                IconButton(onClick = onLanguageClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
            SettingItem(
                itemName = stringResource(R.string.refresh_data),
                description = stringResource(R.string.refresh_data_desc)
            ) {
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
