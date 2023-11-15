package com.zzy.champions.ui.settings.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.model.SettingSelectable
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onBack: (String, String) -> Unit
) {
    val appVersion by viewModel.appVersion.collectAsStateWithLifecycle()
    val versions by viewModel.availableVersions.collectAsStateWithLifecycle()

    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = 1) {
        viewModel.initData()
    }

    BackHandler {
        onBack(appVersion, language)
    }

    SettingsContent(
        modifier = modifier,
        gameVersion = appVersion,
        dataVersions = if (versions is UiState.Success) (versions as UiState.Success).data else listOf(
            SettingSelectable(appVersion, true)
        ),
        onAppVersionSelected = {
            viewModel.saveVersion(it)
        },
        languages = if (languages is UiState.Success) (languages as UiState.Success).data else listOf(SettingSelectable(language, true)),
        onLanguageSelected = {
            viewModel.saveLanguage(it)
        },
        onBack = {
            onBack(appVersion, language)
        },
        showError = versions is UiState.Error || languages is UiState.Error
    )
}