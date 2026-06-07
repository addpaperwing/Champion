package com.zzy.champions.items

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.ui.items.compose.categorizeItems
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

    @Test
    fun bootsTagged_goesToBootsCategory() {
        val boots = item("3020", listOf("Boots"), 1100)
        val result = categorizeItems(listOf(boots))
        val bootsCat = result.find { it.first == "Boots" }
        assertTrue(bootsCat?.second?.contains(boots) == true)
    }

    @Test
    fun mythicTagged_goesToMythicCategory() {
        val mythic = item("6632", listOf("Damage", "Mythic"), 3300)
        val result = categorizeItems(listOf(mythic))
        val mythicCat = result.find { it.first == "Mythic" }
        assertTrue(mythicCat?.second?.contains(mythic) == true)
    }

    @Test
    fun legendaryTagged_goesToLegendaryCategory() {
        val legendary = item("3031", listOf("Damage", "Legendary"), 3400)
        val result = categorizeItems(listOf(legendary))
        val legCat = result.find { it.first == "Legendary" }
        assertTrue(legCat?.second?.contains(legendary) == true)
    }

    @Test
    fun itemUsedAsComponent_goesToComponentsCategory() {
        val component = item("1036", listOf("Damage"), 350)
        val parent = item("3153", listOf("Damage", "Legendary"), 3300, components = listOf("1036"))
        val result = categorizeItems(listOf(component, parent))
        val compCat = result.find { it.first == "Components" }
        assertTrue(compCat?.second?.contains(component) == true)
    }

    @Test
    fun lowCostNonComponent_goesToStarterCategory() {
        val starter = item("1054", listOf("Health"), 400)  // not a component of any item in the list
        val result = categorizeItems(listOf(starter))
        val starterCat = result.find { it.first == "Starter" }
        assertTrue(starterCat?.second?.contains(starter) == true)
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
        val result = categorizeItems(items)
        val allAssigned = result.flatMap { it.second }
        assertEquals(items.size, allAssigned.size)
        assertEquals(items.size, allAssigned.distinctBy { it.id }.size)
    }
}
