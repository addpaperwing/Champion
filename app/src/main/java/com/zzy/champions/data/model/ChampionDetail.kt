package com.zzy.champions.data.model

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
@JsonClass(generateAdapter = true)
data class ChampionDetail(
    val id: String,
    val skins: List<SkinNumber>,
    val lore: String,
    val spells: List<Spell>,
    val passive: Passive,
    var splashIndex: Int = 0
) {

//    fun getSkinNames(skinNum: Int) = skins[skinNum].name
    fun getSplash(skinNum: Int = splashIndex): String = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${id}_$skinNum.jpg"

//    fun getBanner(index: Int) = if (index == 0) R.drawable.splash_aatrox_0 else getFakeSkinSplash(skins[index - 1].num)


    fun getAbilities() = buildList {
        add(passive)
        addAll(spells)
    }
}