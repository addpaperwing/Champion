package com.zzy.champions.data.local

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.zzy.champions.data.local.FakeData.championDetail
import com.zzy.champions.data.local.FakeData.champions
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG_ARAM
import com.zzy.champions.data.model.NAME_OF_BUILD_UGG
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Spell
import com.zzy.champions.data.model.Stats
import com.zzy.champions.data.model.URL_OF_OPGG
import com.zzy.champions.data.model.URL_OF_OPGG_ARAM
import com.zzy.champions.data.model.URL_OF_UGG
import com.zzy.champions.ui.theme.ASSASSIN
import com.zzy.champions.ui.theme.FIGHTER
import com.zzy.champions.ui.theme.MAGE
import com.zzy.champions.ui.theme.MARKSMAN
import com.zzy.champions.ui.theme.SUPPORT
import com.zzy.champions.ui.theme.TANK
import java.math.BigDecimal

class ChampionDataPreviewParameterProvider: PreviewParameterProvider<ChampionData> {

    override val values: Sequence<ChampionData> = sequenceOf(ChampionData("1.23.4", champions = champions))
}

class ChampionAndDetailPreviewParameterProvider: PreviewParameterProvider<ChampionAndDetail> {

    override val values: Sequence<ChampionAndDetail> = sequenceOf(ChampionAndDetail(champion = champions[2], detail = championDetail))
}

class ChampionBuildsPreviewParameterProvider: PreviewParameterProvider<List<ChampionBuild>> {

    override val values: Sequence<List<ChampionBuild>> = sequenceOf(
        listOf(
            ChampionBuild(NAME_OF_BUILD_OPGG, URL_OF_OPGG),
            ChampionBuild(NAME_OF_BUILD_UGG, URL_OF_UGG),
            ChampionBuild(NAME_OF_BUILD_OPGG_ARAM, URL_OF_OPGG_ARAM)
        )
    )
}

object FakeData {

    val champions = listOf(
        Champion(
            id = "Aatrox",
            name = "Aatrox",
            title = "the Darkin Blade",
            info = Info(attack = 8, defense = 4, magic = 3, difficulty = 4),
            image = Image("Aatrox.png"),
            tags = listOf(FIGHTER, TANK),
            partype = "Blood Well",
            stats = Stats(hp = BigDecimal(580), hpperlevel = BigDecimal(90), mp = BigDecimal(0), mpperlevel = BigDecimal(0), movespeed = BigDecimal(345), armor = BigDecimal(38), armorperlevel = BigDecimal(3.25), spellblock = BigDecimal(32), spellblockperlevel = BigDecimal(1.25), attackrange = BigDecimal(175), hpregen = BigDecimal(3), hpregenperlevel = BigDecimal(1), mpregen = BigDecimal(0), mpregenperlevel = BigDecimal(0), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(60), attackdamageperlevel = BigDecimal(5), attackspeedperlevel = BigDecimal(2.5), attackspeed = BigDecimal(0.651))
        ),
        Champion(
            id = "Ahri",
            name = "Ahri",
            title = "the Nine-Tailed Fox",
            info = Info(attack = 3, defense = 4, magic = 8, difficulty = 5),
            image = Image("Ahri.png"),
            tags = listOf(MAGE, ASSASSIN),
            partype = "Mana",
            stats = Stats(hp = BigDecimal(500), hpperlevel = BigDecimal(82), mp = BigDecimal(418), mpperlevel = BigDecimal(25), movespeed = BigDecimal(330), armor = BigDecimal(18), armorperlevel = BigDecimal(3.5), spellblock = BigDecimal(30), spellblockperlevel = BigDecimal(0.5), attackrange = BigDecimal(550), hpregen = BigDecimal(2.5), hpregenperlevel = BigDecimal(0.6), mpregen = BigDecimal(8), mpregenperlevel = BigDecimal(0.8), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(53), attackdamageperlevel = BigDecimal(3), attackspeedperlevel = BigDecimal(2), attackspeed = BigDecimal(0.668))
        ),
        Champion(
            id = "Akali",
            name = "Akali",
            title = "the Rogue Assassin",
            info = Info(attack = 5, defense = 3, magic = 8, difficulty = 7),
            image = Image("Akali.png"),
            tags = listOf(ASSASSIN),
            partype = "Energy",
            stats = Stats(hp = BigDecimal(500), hpperlevel = BigDecimal(105), mp = BigDecimal(200), mpperlevel = BigDecimal(0), movespeed = BigDecimal(345), armor = BigDecimal(23), armorperlevel = BigDecimal(3.5), spellblock = BigDecimal(37), spellblockperlevel = BigDecimal(1.25), attackrange = BigDecimal(125), hpregen = BigDecimal(9), hpregenperlevel = BigDecimal(0.9), mpregen = BigDecimal(50), mpregenperlevel = BigDecimal(0), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(62), attackdamageperlevel = BigDecimal(3.3), attackspeedperlevel = BigDecimal(3.2), attackspeed = BigDecimal(0.625))
        ),
        Champion(
            id = "Akshan",
            name = "Akshan",
            title = "the Rogue Sentinel",
            info = Info(attack = 0, defense = 0, magic = 0, difficulty = 0),
            image = Image("Akshan.png"),
            tags = listOf(MARKSMAN, ASSASSIN),
            partype = "Mana",
            stats = Stats(hp = BigDecimal(560), hpperlevel = BigDecimal(90), mp = BigDecimal(350), mpperlevel = BigDecimal(40), movespeed = BigDecimal(330), armor = BigDecimal(26), armorperlevel = BigDecimal(3), spellblock = BigDecimal(30), spellblockperlevel = BigDecimal(0.5), attackrange = BigDecimal(500), hpregen = BigDecimal(3.75), hpregenperlevel = BigDecimal(0.65), mpregen = BigDecimal(8.175), mpregenperlevel = BigDecimal(0.7), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(52), attackdamageperlevel = BigDecimal(3.5), attackspeedperlevel = BigDecimal(4), attackspeed = BigDecimal(0.638))
        ),
        Champion(
            id = "Alistar",
            name = "Alistar",
            title = "the Minotaur",
            info = Info(attack = 6, defense = 9, magic = 5, difficulty = 7),
            image = Image("Alistar.png"),
            tags = listOf(TANK, SUPPORT),
            partype = "Mana",
            stats = Stats(hp = BigDecimal(600), hpperlevel = BigDecimal(106), mp = BigDecimal(350), mpperlevel = BigDecimal(40), movespeed = BigDecimal(330), armor = BigDecimal(44), armorperlevel = BigDecimal(3.5), spellblock = BigDecimal(32), spellblockperlevel = BigDecimal(1.25), attackrange = BigDecimal(125), hpregen = BigDecimal(8.5), hpregenperlevel = BigDecimal(0.85), mpregen = BigDecimal(8.5), mpregenperlevel = BigDecimal(0.8), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(62), attackdamageperlevel = BigDecimal(3.75), attackspeedperlevel = BigDecimal(2.125), attackspeed = BigDecimal(0.625))
        ),
        Champion(
            id = "Bard",
            name = "Bard",
            title = "the Wandering Caretaker",
            info = Info(attack = 4, defense = 4, magic = 5, difficulty = 9),
            image = Image("Bard.png"),
            tags = listOf(SUPPORT, MAGE),
            partype = "Mana",
            stats = Stats(hp = BigDecimal(560), hpperlevel = BigDecimal(89), mp = BigDecimal(350), mpperlevel = BigDecimal(50), movespeed = BigDecimal(330), armor = BigDecimal(34), armorperlevel = BigDecimal(4), spellblock = BigDecimal(30), spellblockperlevel = BigDecimal(0.5), attackrange = BigDecimal(500), hpregen = BigDecimal(5.5), hpregenperlevel = BigDecimal(0.55), mpregen = BigDecimal(6), mpregenperlevel = BigDecimal(0.45), crit = BigDecimal(0), critperlevel = BigDecimal(0), attackdamage = BigDecimal(52), attackdamageperlevel = BigDecimal(3), attackspeedperlevel = BigDecimal(2), attackspeed = BigDecimal(0.625))
        ),
    )

    val championDetail = ChampionDetail(
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
}