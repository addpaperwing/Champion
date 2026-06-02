package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.settings.compose.LanguageRoute
import com.zzy.champions.ui.settings.compose.SettingsRoute

const val SETTINGS_ROUTE = "settings"
const val LANGUAGE_ROUTE = "settings/language"

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    composable(
        route = SETTINGS_ROUTE,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseOut))
        },
    ) {
        SettingsRoute(
            onBack = onBack,
            onLanguageClick = onLanguageClick,
            onRefreshDone = onRefreshDone,
        )
    }
}

fun NavGraphBuilder.languageScreen(
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit,
) {
    composable(
        route = LANGUAGE_ROUTE,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseOut))
        },
    ) {
        LanguageRoute(
            onBack = onBack,
            onLanguageSelected = onLanguageSelected,
        )
    }
}
