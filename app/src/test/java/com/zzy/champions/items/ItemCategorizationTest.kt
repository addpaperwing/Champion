package com.zzy.champions.items

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.ui.items.ItemGroup
import com.zzy.champions.ui.items.categorizeItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemCategorizationTest {

    private fun item(id: String, tags: List<String>, total: Int, components: List<String> = emptyList()) = Item(
        id = id, name = id, description = "", plaintext = "",
        image = Image("$id.png"),
        gold = ItemGold(total = total, purchasable = true),
        tags = tags,
        maps = mapOf("11" to true),
        stats = emptyMap(),
        components = components,
    )

    private fun single(item: Item) = ItemGroup(listOf(item))

    @Test
    fun bootsTagged_goesToBootsCategory() {
        val boots = item("3020", listOf("Boots"), 1100)
        val result = categorizeItems(listOf(single(boots)))
        val bootsCat = result.find { it.first == "Boots" }
        assertTrue(bootsCat?.second?.any { it.primary == boots } == true)
    }

    @Test
    fun mythicTagged_goesToMythicCategory() {
        val mythic = item("6632", listOf("Damage", "Mythic"), 3300)
        val result = categorizeItems(listOf(single(mythic)))
        val mythicCat = result.find { it.first == "Mythic" }
        assertTrue(mythicCat?.second?.any { it.primary == mythic } == true)
    }

    @Test
    fun legendaryTagged_goesToLegendaryCategory() {
        val legendary = item("3031", listOf("Damage", "Legendary"), 3400)
        val result = categorizeItems(listOf(single(legendary)))
        val legCat = result.find { it.first == "Legendary" }
        assertTrue(legCat?.second?.any { it.primary == legendary } == true)
    }

    @Test
    fun itemUsedAsComponent_goesToComponentsCategory() {
        val component = item("1036", listOf("Damage"), 350)
        val parent = item("3153", listOf("Damage", "Legendary"), 3300, components = listOf("1036"))
        val result = categorizeItems(listOf(single(component), single(parent)))
        val compCat = result.find { it.first == "Components" }
        assertTrue(compCat?.second?.any { it.primary == component } == true)
    }

    @Test
    fun lowCostNonComponent_goesToStarterCategory() {
        val starter = item("1054", listOf("Health"), 400)  // not a component of any item in the list
        val result = categorizeItems(listOf(single(starter)))
        val starterCat = result.find { it.first == "Starter" }
        assertTrue(starterCat?.second?.any { it.primary == starter } == true)
    }

    @Test
    fun multiVariantGroup_categorizedOnceByPrimarysGold() {
        // Arena's cheaper 2500g variant would fall under "Epic" too (>=1000), but categorization
        // must happen once per merged group (using the primary variant), not once per variant —
        // otherwise the same conceptual item could be filed under two different categories.
        val srVariant = item("3031", listOf("Damage", "Legendary"), 3400)
        val arenaVariant = Item(
            id = "223031", name = "3031", description = "", plaintext = "",
            image = srVariant.image,
            gold = ItemGold(total = 2500, purchasable = true),
            tags = listOf("Damage", "Legendary"),
            maps = mapOf("11" to false, "30" to true),
            stats = emptyMap(),
        )
        val group = ItemGroup(listOf(srVariant, arenaVariant))

        val result = categorizeItems(listOf(group))

        val matchingCategories = result.filter { (_, groups) -> group in groups }
        assertEquals(1, matchingCategories.size)
        assertEquals(listOf(group), matchingCategories.single().second)
    }

    @Test
    fun componentReferencedOnlyByNonPrimaryVariant_stillGoesToComponentsCategory() {
        // The Arena variant's build path references this component even though the SR
        // (primary, empty-components) variant's build path doesn't — componentIds must scan
        // every variant, not just primary, or the component wrongly falls through to Starter.
        val component = item("1036", listOf("Damage"), 350)
        val srVariant = item("3031", listOf("Damage", "Legendary"), 3400)
        val arenaVariant = Item(
            id = "223031", name = "3031", description = "", plaintext = "",
            image = srVariant.image,
            gold = ItemGold(total = 2500, purchasable = true),
            tags = listOf("Damage", "Legendary"),
            maps = mapOf("11" to false, "30" to true),
            stats = emptyMap(),
            components = listOf("1036"),
        )
        val group = ItemGroup(listOf(srVariant, arenaVariant))

        val result = categorizeItems(listOf(single(component), group))

        val compCat = result.find { it.first == "Components" }
        assertTrue(compCat?.second?.any { it.primary == component } == true)
    }

    @Test
    fun eachItem_appearsInExactlyOneCategory() {
        val items = listOf(
            item("3031", listOf("Damage", "Legendary"), 3400),
            item("1036", listOf("Damage"), 350),
            item("3153", listOf("Damage", "Legendary"), 3300, components = listOf("1036")),
            item("3020", listOf("Boots"), 1100),
            item("1054", listOf("Health"), 400),
        )
        val result = categorizeItems(items.map { single(it) })
        val allAssigned = result.flatMap { it.second }
        assertEquals(items.size, allAssigned.size)
        assertEquals(items.size, allAssigned.distinctBy { it.id }.size)
    }
}
