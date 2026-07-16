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
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    composable(
        route = SETTINGS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(SETTINGS_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(SETTINGS_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        },
    ) {
        SettingsRoute(
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
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION, easing = EaseOut))
        },
    ) {
        LanguageRoute(
            onBack = onBack,
            onLanguageSelected = onLanguageSelected,
        )
    }
}
