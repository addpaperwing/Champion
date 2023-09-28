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
    val splashIndex: Int = 0
) {

    fun getSplash() = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${id}_${splashIndex}.jpg"

//    fun getBanner(index: Int) = if (index == 0) R.drawable.splash_aatrox_0 else getFakeSkinSplash(skins[index - 1].num)

    fun getSkinNames(index: Int) = if (index == 0) null else skins[index - 1].name

//    fun getFakeSkinSplash(num: Int) = when(num){
//        1 -> R.drawable.aatrox_1
//        2 -> R.drawable.aatrox_2
//        3 -> R.drawable.aatrox_3
//        7 -> R.drawable.aatrox_7
//        8 -> R.drawable.aatrox_8
//        9 -> R.drawable.aatrox_9
//        11 -> R.drawable.aatrox_11
//        20 -> R.drawable.aatrox_20
//        21 -> R.drawable.aatrox_21
//        30 -> R.drawable.aatrox_30
//        31 -> R.drawable.aatrox_31
//        else -> R.drawable.splash_aatrox_0
//    }

    fun getAbilities() = buildList {
        add(passive)
        addAll(spells)
    }
}