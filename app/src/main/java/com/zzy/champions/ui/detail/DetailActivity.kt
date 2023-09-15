package com.zzy.champions.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.grid.ChampionDetailScreen
import com.zzy.champions.ui.theme.MyApplicationTheme

class DetailActivity: ComponentActivity() {

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
                    Stats()
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