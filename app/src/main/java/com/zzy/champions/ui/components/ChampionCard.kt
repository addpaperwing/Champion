package com.zzy.champions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.theme.AssassinPurple
import com.zzy.champions.ui.theme.FighterRed
import com.zzy.champions.ui.theme.MageBlue
import com.zzy.champions.ui.theme.MarksmanOrange
import com.zzy.champions.ui.theme.SupportGreen
import com.zzy.champions.ui.theme.TankSliver


internal const val FIGHTER = "Fighter"
internal const val MARKSMAN = "Marksman"
internal const val ASSASSIN = "Assassin"
internal const val MAGE = "Mage"
internal const val SUPPORT = "Support"
internal const val TANK = "Tank"
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
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight(900),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Text(
                text = champion.title.uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
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
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        ))
            }
        }
    }
}