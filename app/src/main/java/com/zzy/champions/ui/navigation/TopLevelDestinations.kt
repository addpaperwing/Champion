package com.zzy.champions.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zzy.champions.R

internal data class NavTab(
    val route: String,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
)

// Single source of truth for top-level tabs. Add new tabs here only — the nav bar derives from
// this list. Data-refresh signaling (AppDataRepository.dataRefreshed) is independent of this
// list — it's collected directly by ChampionViewModel/ItemViewModel, not routed per-tab.
internal val TOP_LEVEL_TABS: List<NavTab> = listOf(
    NavTab(CHAMPION_INDEX_ROUTE, R.drawable.ic_champions, R.string.nav_champions),
    NavTab(ITEMS_ROUTE, R.drawable.ic_items, R.string.nav_items),
    NavTab(SETTINGS_ROUTE, R.drawable.ic_settings, R.string.settings),
)

val TOP_LEVEL_ROUTES: Set<String> = TOP_LEVEL_TABS.map { it.route }.toSet()
