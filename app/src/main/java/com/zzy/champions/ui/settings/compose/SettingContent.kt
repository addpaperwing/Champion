package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.data.model.SettingSelectable
import com.zzy.champions.ui.components.ErrorBar
import com.zzy.champions.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appbar(modifier: Modifier = Modifier, onBack: () -> Unit) {
    TopAppBar(modifier = modifier, title = {
        Text(text = stringResource(id = R.string.settings))
    },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}
@Composable
fun SettingItem(modifier: Modifier = Modifier, itemName: String, content: @Composable RowScope.() -> Unit) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = itemName,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground
            )
            content()
        }
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    gameVersion: String,
    dataVersions: List<SettingSelectable>,
    onAppVersionSelected: (String) -> Unit,
    languages: List<SettingSelectable>,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit,
    showError: Boolean = false
) {
    Scaffold(topBar = {
        Appbar(onBack = onBack)
    }) { padding ->
        Column(modifier.padding(padding)) {
            if (showError) ErrorBar()
            SettingItem(
                itemName = stringResource(id = R.string.latest_game_version)
            ) {
                Text(
                    text = gameVersion,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            SettingItem(
                itemName = stringResource(id = R.string.app_data_version)
            ) {
                SelectableBottomMenu(
                    list = dataVersions, onItemSelected = onAppVersionSelected
                )
            }
            SettingItem(
                itemName = stringResource(id = R.string.language)
            ) {
                SelectableBottomMenu(
                    list = languages, onItemSelected = onLanguageSelected
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewItem() {
    MyApplicationTheme {
        SettingsContent(
            gameVersion = "1.13.1",
            dataVersions = listOf(
                    SettingSelectable("1.13.1", false),
                    SettingSelectable("1.13.2", false),
                    SettingSelectable("1.13.3", true),
                    SettingSelectable("1.13.4", false),
                    SettingSelectable("1.13.5", false),
                    SettingSelectable("1.13.6", false),
                ),
            onAppVersionSelected = {

            },
            languages = emptyList(),
            onLanguageSelected = {

            },
            onBack = {
            
        })
    }
}