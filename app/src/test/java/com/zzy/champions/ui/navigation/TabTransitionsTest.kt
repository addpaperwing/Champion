package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class TabTransitionsTest {

    @Test
    fun exitDirection_forwardToHigherIndexTab_isStart() {
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.Start, tabExitDirection(ITEMS_ROUTE, SETTINGS_ROUTE))
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, SETTINGS_ROUTE))
    }

    @Test
    fun exitDirection_backwardToLowerIndexTab_isEnd() {
        assertEquals(SlideDirection.End, tabExitDirection(ITEMS_ROUTE, CHAMPION_INDEX_ROUTE))
        assertEquals(SlideDirection.End, tabExitDirection(SETTINGS_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.End, tabExitDirection(SETTINGS_ROUTE, CHAMPION_INDEX_ROUTE))
    }

    @Test
    fun enterDirection_arrivingFromLowerIndexTab_isStart() {
        assertEquals(SlideDirection.Start, tabEnterDirection(ITEMS_ROUTE, CHAMPION_INDEX_ROUTE))
        assertEquals(SlideDirection.Start, tabEnterDirection(SETTINGS_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.Start, tabEnterDirection(SETTINGS_ROUTE, CHAMPION_INDEX_ROUTE))
    }

    @Test
    fun enterDirection_arrivingFromHigherIndexTab_isEnd() {
        assertEquals(SlideDirection.End, tabEnterDirection(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.End, tabEnterDirection(ITEMS_ROUTE, SETTINGS_ROUTE))
        assertEquals(SlideDirection.End, tabEnterDirection(CHAMPION_INDEX_ROUTE, SETTINGS_ROUTE))
    }

    @Test
    fun exitDirection_unknownTargetRoute_defaultsToStart() {
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, "unknown"))
    }

    @Test
    fun exitDirection_toPushedChildScreen_isStart() {
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, CHAMPION_DETAIL_ROUTE))
        assertEquals(SlideDirection.Start, tabExitDirection(SETTINGS_ROUTE, LANGUAGE_ROUTE))
    }

    @Test
    fun enterDirection_fromPushedChildScreen_isEnd() {
        assertEquals(SlideDirection.End, tabEnterDirection(CHAMPION_INDEX_ROUTE, CHAMPION_DETAIL_ROUTE))
        assertEquals(SlideDirection.End, tabEnterDirection(SETTINGS_ROUTE, LANGUAGE_ROUTE))
    }
}
