package com.zzy.champions.data.local.db

import com.zzy.champions.data.model.ItemGold
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersItemTest {

    @Test
    fun itemGold_roundTrip() {
        val gold = ItemGold(base = 625, purchasable = true, total = 3300, sell = 2310)
        val json = Converters.fromItemGold(gold)
        assertEquals(gold, Converters.toItemGold(json))
    }

    @Test
    fun stringBooleanMap_roundTrip() {
        val map = mapOf("11" to true, "12" to false, "22" to true)
        val json = Converters.fromStringBooleanMap(map)
        assertEquals(map, Converters.toStringBooleanMap(json))
    }

    @Test
    fun stringDoubleMap_roundTrip() {
        val map = mapOf("FlatPhysicalDamageMod" to 40.0, "PercentLifeStealMod" to 0.15)
        val json = Converters.fromStringDoubleMap(map)
        assertEquals(map, Converters.toStringDoubleMap(json))
    }

    @Test
    fun emptyStringBooleanMap_roundTrip() {
        val map = emptyMap<String, Boolean>()
        val json = Converters.fromStringBooleanMap(map)
        assertEquals(map, Converters.toStringBooleanMap(json))
    }

    @Test
    fun emptyStringDoubleMap_roundTrip() {
        val map = emptyMap<String, Double>()
        val json = Converters.fromStringDoubleMap(map)
        assertEquals(map, Converters.toStringDoubleMap(json))
    }
}
