package com.zzy.champions.items

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemGroup
import com.zzy.champions.ui.items.groupItems
import com.zzy.champions.ui.items.isAvailableOnACuratedGameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemGroupingTest {

    private fun item(
        id: String,
        name: String,
        iconFull: String = "$id.png",
        total: Int = 1000,
        maps: Map<String, Boolean> = mapOf(GAME_MODE_SUMMONERS_RIFT to true),
    ) = Item(
        id = id, name = name, description = "", plaintext = "",
        image = Image(iconFull),
        gold = ItemGold(total = total, purchasable = true),
        tags = emptyList(),
        maps = maps,
        stats = emptyMap(),
    )

    @Test
    fun distinctNames_eachBecomeItsOwnSingletonGroup() {
        val sword = item("1036", "Long Sword")
        val shoes = item("3020", "Sorcerer's Shoes")

        val groups = groupItems(listOf(sword, shoes))

        assertEquals(2, groups.size)
        assertTrue(groups.all { it.variants.size == 1 })
    }

    @Test
    fun sameName_mergeIntoOneGroupWithBothVariants_evenWithDifferentIconFilenames() {
        // Mirrors real Data Dragon data: Arena ships a separately-priced/tuned catalog entry
        // for many items, sharing the SR item's name but a different id/gold/maps — and
        // therefore also a different icon filename, since image.full always mirrors the
        // item's own id (e.g. "223031.png" vs "3031.png"). Confirmed against live Data
        // Dragon data: no real mode-variant pair ever shares an icon filename, so grouping
        // must key on name alone, not (name, icon).
        val srVariant = item(
            id = "3031", name = "Infinity Edge", iconFull = "3031.png", total = 3500,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true, GAME_MODE_ARENA to false),
        )
        val arenaVariant = item(
            id = "223031", name = "Infinity Edge", iconFull = "223031.png", total = 2500,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to false, GAME_MODE_ARENA to true),
        )

        val groups = groupItems(listOf(srVariant, arenaVariant))

        assertEquals(1, groups.size)
        assertEquals(setOf(srVariant, arenaVariant), groups.single().variants.toSet())
    }

    @Test
    fun primary_prefersTheShorterId() {
        val arenaVariant = item("223031", "Infinity Edge", maps = mapOf(GAME_MODE_ARENA to true, GAME_MODE_SUMMONERS_RIFT to false))
        val srVariant = item("3031", "Infinity Edge", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true, GAME_MODE_ARENA to false))

        // Order in the input list shouldn't matter — the shorter (canonical) id wins regardless
        // of position.
        val group = ItemGroup(listOf(arenaVariant, srVariant))

        assertEquals(srVariant, group.primary)
    }

    @Test
    fun primary_isDeterministic_evenWhenBothVariantsAreOnSummonersRift() {
        // Real Data Dragon data: Zeke's Convergence ships both "3050" and "323050" marked
        // available on Summoner's Rift, so "prefer the SR variant" can't disambiguate them —
        // it would just pick whichever the DB happened to return first. The shorter id must
        // win regardless of list order.
        val canonical = item("3050", "Zeke's Convergence", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true))
        val reskin = item("323050", "Zeke's Convergence", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true))

        assertEquals(canonical, ItemGroup(listOf(reskin, canonical)).primary)
        assertEquals(canonical, ItemGroup(listOf(canonical, reskin)).primary)
    }

    @Test
    fun primary_breaksEqualLengthIdTiesDeterministically() {
        val a = item("1101", "Scorchclaw Pup", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to false))
        val b = item("1107", "Scorchclaw Pup", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to false))

        // Same result regardless of input order — "1101" sorts before "1107".
        assertEquals(a, ItemGroup(listOf(a, b)).primary)
        assertEquals(a, ItemGroup(listOf(b, a)).primary)
    }

    @Test
    fun id_isStableRegardlessOfVariantOrder() {
        val a = item("223031", "Infinity Edge")
        val b = item("3031", "Infinity Edge")

        assertEquals(ItemGroup(listOf(a, b)).id, ItemGroup(listOf(b, a)).id)
    }

    @Test
    fun isAvailableOnACuratedGameMode_trueWhenAnyOfTheThreeCuratedModesIsTrue() {
        val srOnly = item("1", "A", maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true))
        val aramOnly = item("2", "B", maps = mapOf(GAME_MODE_ARAM to true))
        val arenaOnly = item("3", "C", maps = mapOf(GAME_MODE_ARENA to true))

        assertTrue(srOnly.isAvailableOnACuratedGameMode())
        assertTrue(aramOnly.isAvailableOnACuratedGameMode())
        assertTrue(arenaOnly.isAvailableOnACuratedGameMode())
    }

    @Test
    fun isAvailableOnACuratedGameMode_falseWhenOnlyAvailableOnANonCuratedMap() {
        // Map "21" is Nexus Blitz — a real Data Dragon map ID this app has no filter chip for.
        // Being available there (and nowhere curated) must not be enough to surface the item.
        val nexusBlitzOnly = item(
            "1200", "Rocket Belt Prototype",
            maps = mapOf(
                GAME_MODE_SUMMONERS_RIFT to false,
                GAME_MODE_ARAM to false,
                GAME_MODE_ARENA to false,
                "21" to true,
            ),
        )

        assertFalse(nexusBlitzOnly.isAvailableOnACuratedGameMode())
    }

    @Test
    fun isAvailableOnACuratedGameMode_falseWhenMapsIsEmpty() {
        val noMaps = item("9999", "Retired Trinket", maps = emptyMap())

        assertFalse(noMaps.isAvailableOnACuratedGameMode())
    }
}
