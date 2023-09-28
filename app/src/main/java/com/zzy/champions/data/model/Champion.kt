package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
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
@Entity(tableName = "champion")
data class Champion(
    @PrimaryKey
    val id: String,
    val name: String,
    val title: String,
    val image: Image,
    val tags: List<String>,
    val partype: String,
    val info: Info,
    val stats: Stats,
//    val spells: List<>,
//    val passive: Any
) {

    companion object {
        var version: String = "3.9.7"
    }

    fun getAvatar() = "https://ddragon.leagueoflegends.com/cdn/$version/img/champion/${image.full}"

//    fun getAbilityImage(index: Int) = when(index) {
//        0 -> getP()
//        1 -> getQ()
//        2 -> getW()
//        3 -> getE()
//        4 -> getR()
//        else -> getP()
//    }
//
//    private fun getP() = "https://ddragon.leagueoflegends.com/cdn/$version/img/passive/${id}_Passive.png"
//    private fun getQ() = "https://ddragon.leagueoflegends.com/cdn/$version/img/spell/${id}Q.png"
//    private fun getW() = "https://ddragon.leagueoflegends.com/cdn/$version/img/spell/${id}W.png"
//    private fun getE() = "https://ddragon.leagueoflegends.com/cdn/$version/img/spell/${id}E.png"
//    private fun getR() = "https://ddragon.leagueoflegends.com/cdn/$version/img/spell/${id}R.png"
}