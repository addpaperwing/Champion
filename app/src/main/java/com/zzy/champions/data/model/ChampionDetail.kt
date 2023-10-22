package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//Aatrox: {
//id: "Aatrox",
//key: "266",
//name: "Aatrox",
//title: "the Darkin Blade",
//skins: [],
//lore: "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance.",
//blurb: "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find...",

//tags: [],
//partype: "Blood Well",
//info: {},
//stats: {},
//spells: [
//{},
//{},
//{},
//{}
//],
//passive: {},
//recommended: [ ]
//}
@Entity
@JsonClass(generateAdapter = true)
data class ChampionDetail(
    //Use different name for one-to-one relationship of Room Database
    @PrimaryKey @Json(name = "id")val championId: String,
    val skins: List<SkinNumber>,
    val lore: String,
    val spells: List<Spell>,
    val passive: Passive,
    var splashIndex: Int = 0
) {
    fun getSplash(skinNum: Int = splashIndex): String = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${championId}_$skinNum.jpg"

    fun getAbilities() = buildList {
        add(passive)
        addAll(spells)
    }
}