package com.zzy.champions

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.data.repository.ItemRepository

internal val longSword = Item(
    id = "1036",
    name = "Long Sword",
    description = "A long sword",
    plaintext = "Slightly increases Attack Damage",
    image = Image("1036.png"),
    gold = ItemGold(base = 350, purchasable = true, total = 350, sell = 245),
    tags = listOf("Damage"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatPhysicalDamageMod" to 10.0),
    components = emptyList(),
    upgrades = listOf("3153"),
)

internal val infinityEdge = Item(
    id = "3031",
    name = "Infinity Edge",
    description = "Critical strikes deal bonus damage",
    plaintext = "Greatly increases Attack Damage and Critical Strike Damage",
    image = Image("3031.png"),
    gold = ItemGold(base = 625, purchasable = true, total = 3400, sell = 2380),
    tags = listOf("Damage", "CriticalStrike", "Legendary"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatPhysicalDamageMod" to 80.0, "FlatCritChanceMod" to 0.2),
    components = listOf("1038", "1018"),
    upgrades = emptyList(),
)

internal val sorceresShoes = Item(
    id = "3020",
    name = "Sorcerer's Shoes",
    description = "Increases magic penetration",
    plaintext = "Enhances Move Speed and magic penetration",
    image = Image("3020.png"),
    gold = ItemGold(base = 600, purchasable = true, total = 1100, sell = 770),
    tags = listOf("Boots", "SpellDamage"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatMovementSpeedMod" to 45.0, "FlatMagicPenetrationMod" to 18.0),
    components = listOf("1001"),
    upgrades = emptyList(),
)

internal class TestItemRepository : ItemRepository {
    private val items = mutableListOf(longSword, infinityEdge, sorceresShoes)
    var shouldThrowOnFetch = false

    override suspend fun getRemoteItems(version: String, language: String): List<Item> {
        if (shouldThrowOnFetch) throw java.io.IOException("Network error")
        return items
    }

    override suspend fun saveLocalItems(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
    }

    override suspend fun getLocalItems(): List<Item> = items.toList()

    override suspend fun getItemCount(): Int = items.size

    override suspend fun getItemById(id: String): Item? = items.find { it.id == id }
}
