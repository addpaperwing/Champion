package com.zzy.champions.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelDestinationsTest {

    @Test
    fun topLevelTabs_includesSettingsAsThirdTab() {
        assertEquals(3, TOP_LEVEL_TABS.size)
        assertEquals(SETTINGS_ROUTE, TOP_LEVEL_TABS.last().route)
    }

    @Test
    fun topLevelRoutes_includesSettingsRoute() {
        assertTrue(SETTINGS_ROUTE in TOP_LEVEL_ROUTES)
    }
}
