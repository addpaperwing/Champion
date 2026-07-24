package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val id: String = "",        // set from map key in repository; empty default lets Moshi skip it
    val name: String = "",
    val description: String = "",
    val plaintext: String = "",
    val image: Image = Image(""),
    val gold: ItemGold = ItemGold(),
    val tags: List<String> = emptyList(),
    val maps: Map<String, Boolean> = emptyMap(),
    val stats: Map<String, Double> = emptyMap(),
    @Json(name = "from") val components: List<String> = emptyList(),  // "from" is SQL reserved word
    @Json(name = "into") val upgrades: List<String> = emptyList(),    // renamed for clarity
    val inStore: Boolean = true, // absent in Data Dragon JSON means true; only ever appears as false
) {
    fun getIconUrl(version: String) = itemIconUrl(version, id)
}
