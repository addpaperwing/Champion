package com.zzy.champions.ui.settings.compose

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.R
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.compose.TextDialog
import com.zzy.champions.ui.settings.SettingsViewModel
import java.util.Locale

private fun localeFromTag(tag: String): Locale = Locale.forLanguageTag(tag.replace('_', '-'))

private fun Locale.selfDisplayName(): String =
    getDisplayName(this).replaceFirstChar { it.uppercaseChar() }

@Composable
fun LanguageRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit,
) {
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var pendingLanguage by remember { mutableStateOf<String?>(null) }

    pendingLanguage?.let { lang ->
        val (locale, displayName) = remember(lang) {
            val l = localeFromTag(lang)
            l to l.selfDisplayName()
        }
        TextDialog(
            onDismissRequest = { pendingLanguage = null },
            title = stringResource(R.string.switch_language_dialog_title),
            positiveButtonText = stringResource(R.string.confirm),
            negativeButtonText = stringResource(R.string.cancel),
            onPositiveButtonClick = {
                pendingLanguage = null
                viewModel.selectLanguage(lang, onDone = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
                    onLanguageSelected()
                })
            },
            onNegativeButtonClick = { pendingLanguage = null }
        ) {
            Text(
                text = stringResource(R.string.switch_language_dialog_message, displayName),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { LanguageAppbar(onBack = onBack) }
    ) { padding ->
        when (languages) {
            is UiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(stringResource(R.string.internet_connection_error)) }

            is UiState.Success -> LazyColumn(Modifier.padding(padding)) {
                items((languages as UiState.Success).data) { lang ->
                    LanguageItem(
                        language = lang,
                        isSelected = lang == currentLanguage,
                        onClick = { pendingLanguage = lang }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageAppbar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.select_language)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun LanguageItem(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val displayName = remember(language) { localeFromTag(language).selfDisplayName() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = language,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
