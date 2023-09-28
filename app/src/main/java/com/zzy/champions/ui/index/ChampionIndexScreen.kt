package com.zzy.champions.ui.index


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.AssassinPurple
import com.zzy.champions.ui.theme.FighterRed
import com.zzy.champions.ui.theme.MageBlue
import com.zzy.champions.ui.theme.MarksmanOrange
import com.zzy.champions.ui.theme.MyApplicationTheme
import com.zzy.champions.ui.theme.SupportGreen
import com.zzy.champions.ui.theme.TankSliver

internal const val FIGHTER = "Fighter"
internal const val MARKSMAN = "Marksman"
internal const val ASSASSIN = "Assassin"
internal const val MAGE = "Mage"
internal const val SUPPORT = "Support"
internal const val TANK = "Tank"


@Composable
fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.choose_your).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 10.sp
        )
        Text(
            text = stringResource(id = R.string.champion).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 36.sp,
            fontWeight = FontWeight(900),
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.choose_your_champion_desc),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 8.sp,
            lineHeight = 10.sp
        )
        SearchBar()
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    TextField(
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp))
            .background(Color.Transparent)
            .border(1.dp, Color.Black),
        value = "", onValueChange = { newValue ->

        })
}



@Composable
fun ChampionCard(
    modifier: Modifier = Modifier,
    champion: Champion,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                Dp.Hairline,
                Color.White,
                shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp)
            )
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth()
                .background(
                    when (champion.tags[0]) {
                        FIGHTER -> FighterRed
                        ASSASSIN -> AssassinPurple
                        MAGE -> MageBlue
                        MARKSMAN -> MarksmanOrange
                        SUPPORT -> SupportGreen
                        TANK -> TankSliver
                        else -> TankSliver
                    }
                )
                .clip(CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp))
                .padding(6.dp)
        ) {
            Text(
                text = champion.name.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight(900),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Text(
                text = champion.title.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 8.sp,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight(700),
                textAlign = TextAlign.End
            )
            Row {
                AsyncImage(
                    model = champion.getAvatar(),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(corner = CornerSize(6.dp)))
                        .size(72.dp),

                    contentDescription = ""
                )
                RoleAndDifficulty(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .weight(1f)
                        .height(72.dp),
                    role = champion.tags[0],
                    difficulty = champion.info.difficulty
                )
            }
        }
    }
}

@Composable
fun RoleAndDifficulty(modifier: Modifier = Modifier , role: String, difficulty: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = stringResource(id = R.string.role).uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight(600),
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
        Text(
            text = role.uppercase(),
            color = Color(0xffd0a85c),
            fontWeight = FontWeight(600),
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
        Difficulty(modifier = Modifier.padding(top = 6.dp), difficulty = difficulty)
    }
}

@Composable
fun Difficulty(modifier : Modifier= Modifier, difficulty: Int) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = stringResource(id = R.string.difficulty).uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight(600),
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
        Row {
            (0..2).forEach { times ->
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(20.dp, 6.dp)
                        .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
                        .background(
                            if (difficulty - (times * 3.5).toInt() > 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                        ))
            }
        }
    }
}

@Composable
fun ChampionsGrid(
    modifier: Modifier = Modifier,
    champions: List<Champion>,
    onItemClick: (Champion) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(all = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Header()
        }
        items(count = champions.size) { index ->
            ChampionCard(champion = champions[index]) {
                onItemClick(champions[index])
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChampionGrid() {
    val aatrox = Champion(
        "Aatrox",
        "Aatrox",
        "the Darkin Blade",
        Image(""),
        listOf(TANK, SUPPORT),
        "",
        Info(difficulty = 7),
        Stats()
    )

    MyApplicationTheme {
        val champions: ArrayList<Champion> = ArrayList()

        for (i in 0..10) {
            champions.add(aatrox)
        }
        Scaffold { padding ->
            ChampionsGrid(modifier = Modifier.padding(padding), champions = champions) {

            }
        }
    }
}