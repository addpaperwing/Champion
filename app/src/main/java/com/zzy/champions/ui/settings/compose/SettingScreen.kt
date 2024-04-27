package com.zzy.champions.ui.settings.compose

//@Composable
//fun SettingsScreen(
//    modifier: Modifier = Modifier,
//    viewModel: SettingsViewModel,
//    onBack: (String, String) -> Unit
//) {
//    val appVersion by viewModel.appVersion.collectAsStateWithLifecycle()
//    val versions by viewModel.availableVersions.collectAsStateWithLifecycle()
//
//    val languages by viewModel.languages.collectAsStateWithLifecycle()
//    val language by viewModel.language.collectAsStateWithLifecycle()
//
//    LaunchedEffect(key1 = 1) {
//        viewModel.initData()
//    }
//
//    BackHandler {
//        onBack(appVersion, language)
//    }
//
//    Settings(
//        modifier = modifier,
//        gameVersion = appVersion,
//        dataVersions = if (versions is UiState.Success) (versions as UiState.Success).data else listOf(
//            SettingSelectable(appVersion, true)
//        ),
//        onAppVersionSelected = {
//            viewModel.saveVersion(it)
//        },
//        languages = if (languages is UiState.Success) (languages as UiState.Success).data else listOf(SettingSelectable(language, true)),
//        onLanguageSelected = {
//            viewModel.saveLanguage(it)
//        },
//        showError = versions is UiState.Error || languages is UiState.Error
//    )
//}