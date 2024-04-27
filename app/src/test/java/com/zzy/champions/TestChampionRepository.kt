package com.zzy.champions

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Spell
import com.zzy.champions.data.model.Stats
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.DEFAULT_EARLIEST_VERSION
import com.zzy.champions.ui.theme.ASSASSIN
import com.zzy.champions.ui.theme.FIGHTER
import com.zzy.champions.ui.theme.MAGE
import com.zzy.champions.ui.theme.TANK
import java.io.IOException

internal const val VERSION_14_0 = "14.0.0"
internal const val VERSION_14_1 = "14.1.0"
internal const val ERROR_VERSION = "4.0.0"
internal const val LANGUAGE_US = "US"

internal val aatrox = Champion(
                            id = "Aatrox",
                            name = "Aatrox",
                            title = "the Darkin Blade",
                            info = Info(attack = 8, defense = 4, magic = 3, difficulty = 4),
                            image = Image("Aatrox.png"),
                            tags = listOf(FIGHTER, TANK),
                            partype = "Blood Well",
                            stats = Stats()
                        )
internal val ahri = Champion(
                            id = "Ahri",
                            name = "Ahri",
                            title = "the Nine-Tailed Fox",
                            info = Info(attack = 3, defense = 4, magic = 8, difficulty = 5),
                            image = Image("Ahri.png"),
                            tags = listOf(MAGE, ASSASSIN),
                            partype = "Mana",
                            stats = Stats()
                        )
internal val akali = Champion(
        id = "Akali",
        name = "Akali",
        title = "the Rogue Assassin",
        info = Info(attack = 5, defense = 3, magic = 8, difficulty = 7),
        image = Image("Akali.png"),
        tags = listOf(ASSASSIN),
        partype = "Energy",
        stats = Stats()
    )
internal val akaliDetail = ChampionDetail(
        championId = "Akali",
        skins = listOf(
            SkinNumber(0, "default"),
            SkinNumber(1, "Stinger Akali"),
            SkinNumber(2, "Infernal Akali"),
            SkinNumber(3, "All-star Akali"),
            SkinNumber(4, "Nurse Akali"),
            SkinNumber(5, "Blood Moon Akali"),
            SkinNumber(6, "Silverfang Akali"),
            SkinNumber(7, "Headhunter Akali"),
            SkinNumber(8, "Sashimi Akali"),
            SkinNumber(9, "K/DA Akali"),
            SkinNumber(13, "Prestige K/DA Akali"),
            SkinNumber(14, "PROJECT: Akali"),
            SkinNumber(15, "True Damage Akali"),
            SkinNumber(32, "K/DA ALL OUT Akali"),
            SkinNumber(50, "Crime City Nightmare Akali"),
            SkinNumber(60, "Prestige K/DA Akali (2022)"),
            SkinNumber(61, "Star Guardian Akali"),
            SkinNumber(68, "DRX Akali"),
        ),
        lore =  "Abandoning the Kinkou Order and her title of the Fist of Shadow, Akali now strikes alone, ready to be the deadly weapon her people need. Though she holds onto all she learned from her master Shen, she has pledged to defend Ionia from its enemies, one kill at a time. Akali may strike in silence, but her message will be heard loud and clear: fear the assassin with no master.",
        spells = listOf(
            Spell(name =  "Five Point Strike",
            description = "Akali throws out five kunai, dealing damage based on her bonus Attack Damage and Ability Power and slowing.",
            image = Image(
                "AkaliQ.png"
            )),
            Spell(name = "Twilight Shroud",
            description = "Akali throws out five kunai, dealing damage based on her bonus Attack Damage and Ability Power and slowing.",
            image = Image(
                "AkaliW.png"
            )),
            Spell(name = "Shuriken Flip",
            description =  "Flip backward and fire a shuriken forward, dealing magic damage. The first enemy or smoke cloud hit is marked. Re-cast to dash to the marked target, dealing additional damage.",
            image = Image(
                "AkaliE.png"
            )),
            Spell(name = "Perfect Execution",
            description = "Akali leaps in a direction, damaging enemies she strikes. Re-cast: Akali dashes in a direction, executing all enemies she strikes.",
            image = Image(
                "AkaliR.png"
            )),
        ),
        passive = Passive(
            name = "Assassin's Mark",
            description = "Dealing spell damage to a champion creates a ring of energy around them. Exiting that ring empowers Akali's next Attack with bonus range and damage.",
            image = Image(
                "Akali_P.png"
            )
        ),
        splashIndex = 0
    )

internal val champion = Champion(
    id = "",
    name = "",
    title = "",
    info = Info(),
    image = Image(""),
    tags = emptyList(),
    partype = "",
    stats = Stats()
)
internal val championDetail = ChampionDetail(
        championId = "",
        skins = emptyList(),
        lore =  "",
        spells = emptyList(),
        passive = Passive(name = "", description = "", image = Image("")),
        splashIndex = 0
)
class TestChampionRepository: ChampionRepository {

    private var localChampions = emptyList<Champion>()


    private val localChampionDetails: HashMap<String, ChampionDetail> = HashMap()

    override suspend fun getRemoteChampions(version: String, language: String): ChampionResponse {
        when (version) {
            VERSION_14_0 -> {
                return ChampionResponse(
                    mapOf(
                        aatrox.name to aatrox
                    )
                )
            }
            VERSION_14_1 -> {
                return ChampionResponse(
                    mapOf(
                        ahri.name to ahri
                    )
                )
            }
            DEFAULT_EARLIEST_VERSION -> {
                return ChampionResponse(mapOf(
                    akali.name to akali
                ))
            }
            ERROR_VERSION -> {
                throw IOException()
            }
            else -> {
                return ChampionResponse(emptyMap())
            }
        }
    }

    override suspend fun saveLocalChampions(champions: List<Champion>) {
        localChampions = champions
    }

    override suspend fun searchChampionsBy(id: String): List<Champion> {
        return localChampions.filter { it.id.contains(id) }
    }

    override suspend fun getRemoteChampionDetail(
        version: String,
        language: String,
        id: String
    ): ChampionDetail? = if (id == akaliDetail.championId) {
        akaliDetail
    } else null

    override suspend fun getLocalChampionDetail(championId: String): ChampionDetail? = localChampionDetails[championId]

    override suspend fun saveChampionDetail(championDetail: ChampionDetail) {
        localChampionDetails[championDetail.championId] = championDetail
    }

    override suspend fun getChampionAndDetail(id: String): ChampionAndDetail = if (id == akali.id) {
        ChampionAndDetail(akali, getLocalChampionDetail(id)?: championDetail)
    } else {
        ChampionAndDetail(champion, championDetail)
    }
}