package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection

// A route not present in TOP_LEVEL_TABS is a pushed child screen (e.g. a
// detail or settings sub-screen), not a sibling tab. Such screens are always
// reached by pushing forward from whichever tab hosts them, so they're
// treated as further forward than every tab (Int.MAX_VALUE) rather than
// further backward (-1). This matches the standard push/pop convention
// already hardcoded on those child screens themselves (e.g.
// championDetailScreen, languageScreen): pushing forward slides Start,
// popping back slides End.
private fun tabIndex(route: String?): Int =
    TOP_LEVEL_TABS.indexOfFirst { it.route == route }.let { if (it == -1) Int.MAX_VALUE else it }

// Direction tabs slide when moving from `sourceRoute` to `destinationRoute`:
// Start when advancing to a tab further right in TOP_LEVEL_TABS or to a
// pushed child screen, End when moving to one further left (or back from a
// pushed child screen). Both the exiting tab and the entering tab use this
// same value for a given transition, which is what keeps the animation
// reading as one continuous slide instead of two screens moving
// independently.
private fun slideDirection(sourceRoute: String?, destinationRoute: String?): SlideDirection =
    if (tabIndex(destinationRoute) > tabIndex(sourceRoute)) SlideDirection.Start else SlideDirection.End

// For a tab's exitTransition: `ownRoute` is the tab being left, `targetRoute`
// is where the user is navigating to.
internal fun tabExitDirection(ownRoute: String, targetRoute: String?): SlideDirection =
    slideDirection(sourceRoute = ownRoute, destinationRoute = targetRoute)

// For a tab's enterTransition: `ownRoute` is the tab being entered,
// `initialRoute` is where the user navigated from.
internal fun tabEnterDirection(ownRoute: String, initialRoute: String?): SlideDirection =
    slideDirection(sourceRoute = initialRoute, destinationRoute = ownRoute)
