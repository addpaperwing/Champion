package com.zzy.champions.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zzy.champions.R

internal data class NavTab(
    val route: String,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val refreshesOnDataChange: Boolean = false,
)

// Single source of truth for top-level tabs. Add new tabs here only —
// signalRefresh() and the nav bar both derive from this list.
// Set refreshesOnDataChange = true only for tabs whose content is fetched from
// the remote data source; local-only tabs should use the default (false).
internal val TOP_LEVEL_TABS: List<NavTab> = listOf(
    NavTab(CHAMPION_INDEX_ROUTE, R.drawable.ic_champions, R.string.nav_champions, refreshesOnDataChange = true),
    NavTab(ITEMS_ROUTE, R.drawable.ic_items, R.string.nav_items, refreshesOnDataChange = true),
)

val TOP_LEVEL_ROUTES: Set<String> = TOP_LEVEL_TABS.map { it.route }.toSet()

internal val REFRESH_TABS: List<NavTab> = TOP_LEVEL_TABS.filter { it.refreshesOnDataChange }
