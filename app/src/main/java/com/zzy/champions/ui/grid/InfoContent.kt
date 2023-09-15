package com.zzy.champions.ui.grid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import java.math.BigDecimal
import kotlin.math.roundToInt

@Composable
fun ChampionLevel(modifier: Modifier = Modifier, champion: Champion) {
     var sliderPosition by remember { mutableFloatStateOf(1f) }
    Column(modifier = modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.level),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier,
                fontSize = 24.sp
            )
            Text(
                text = "${sliderPosition.roundToInt()}",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier,
                fontSize = 36.sp
            )
        }
//        Row {
//            BaseStatsText(
//                text = stringResource(
//                    id = R.string.move_speed,
//                    champion.stats.movespeed
//                )
//            )
//            BaseStatsText(
//                text = stringResource(
//                    id = R.string.attack_range,
//                    champion.stats.attackrange
//                )
//            )
//        }
        Slider(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 16,
            valueRange = 1f..18f
        )
    }
}

@Composable
fun BaseStatsText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .border(
                border = BorderStroke(0.5.dp, Color.White),
                shape = CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp)
            )
            .padding(6.dp),
        fontSize = 12.sp
    )
}

@Preview
@Composable
fun PreviewStats() {
    val champion = Champion(
        "aatrox",
        1,
        "star guardian seraphine The Darkin Blade",
        "The Darkin Blade",
        listOf("Warrior", "Fighter", "Assassin"),
        "Blood Well",
        Info(difficulty = 5),
        Stats(movespeed = BigDecimal(355), attackrange = BigDecimal(120))
    )
    MaterialTheme {
        ChampionLevel(champion = champion)
    }
}