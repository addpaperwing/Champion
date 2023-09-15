package com.zzy.champions.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.math.BigDecimal

class DetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            val systemUiController: SystemUiController = rememberSystemUiController()
            MyApplicationTheme {
                val champion = Champion(
                    "aatrox",
                    1,
                    "Aatrox",
                    "The Darkin Blade",
                    listOf("Warrior", "Fighter", "Assassin"),
                    "Blood Well",
                    Info(difficulty = 5),
                    Stats(
                        hp = BigDecimal(650),
                        hpperlevel = BigDecimal(114),

                        hpregen = BigDecimal(2.5),
                        hpregenperlevel = BigDecimal(0.6),

                        mp = BigDecimal(418),
                        mpperlevel = BigDecimal(25),

                        mpregen = BigDecimal(8),
                        mpregenperlevel = BigDecimal(0.8),

                        movespeed = BigDecimal(355),

                        armor = BigDecimal(21),
                        armorperlevel = BigDecimal(4.7),

                        spellblock = BigDecimal(30),
                        spellblockperlevel = BigDecimal(1.3),


                        attackdamage = BigDecimal(53),
                        attackdamageperlevel = BigDecimal(3),

                        attackspeed = BigDecimal(0.688),
                        attackspeedperlevel = BigDecimal(2),

                        attackrange = BigDecimal(120),
                    )
                )
                val detail = ChampionDetail(
                    "aatrox",
                    emptyList(),
                    "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance.",
                    listOf(
                        Ability(
                            "q",
                            "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. "
                        ),
                        Ability(
                            "w",
                            "But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. "
                        ),
                        Ability(
                            "e",
                            "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. "
                        ),
                        Ability(
                            "r",
                            "But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. "
                        )
                    ),
                    Ability(
                        "p",
                        "Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance."
                    )
                )

                ChampionDetailScreen(champion = champion, detail = detail)
            }
        }
    }
}