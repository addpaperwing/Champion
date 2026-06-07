package com.zzy.champions.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemTest {

    private val testItem = Item(
        id = "3153",
        name = "Blade of the Ruined King",
        description = "<mainText>Deals damage</mainText>",
        plaintext = "Deals bonus damage to high-health enemies",
        image = Image("3153.png"),
        gold = ItemGold(base = 625, purchasable = true, total = 3300, sell = 2310),
        tags = listOf("Damage", "SpellDamage"),
        maps = mapOf("11" to true, "12" to true),
        stats = mapOf("FlatPhysicalDamageMod" to 40.0),
        components = listOf("1043", "3134"),
        upgrades = emptyList(),
    )

    @Test
    fun getIconUrl_returnsCorrectDDragonUrl() {
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/14.1.1/img/item/3153.png",
            testItem.getIconUrl("14.1.1")
        )
    }

    @Test
    fun item_withPurchasableTrue_isPurchasable() {
        assertTrue(testItem.gold.purchasable)
    }

    @Test
    fun item_withPurchasableFalse_isNotPurchasable() {
        assertFalse(testItem.copy(gold = testItem.gold.copy(purchasable = false)).gold.purchasable)
    }
}
