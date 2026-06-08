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
) {
    fun getIconUrl(version: String) =
        "https://ddragon.leagueoflegends.com/cdn/$version/img/item/${image.full}"
}

// For component/upgrade IDs that don't carry an Image object
fun itemIconUrl(version: String, itemId: String) =
    "https://ddragon.leagueoflegends.com/cdn/$version/img/item/$itemId.png"
