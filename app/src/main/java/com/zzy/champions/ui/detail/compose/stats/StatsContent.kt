package com.zzy.champions.ui.detail.compose.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.local.adaptiveScale
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.math.BigDecimal
import kotlin.math.roundToInt

@Composable
fun ChampionLevel(modifier: Modifier = Modifier, level: Float, onLevelChange: (Float) -> Unit) {
    Column(modifier = modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.level),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier,
                fontSize = 24.sp
            )
            Text(
                text = "${(level + 1).roundToInt()}",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier,
                fontSize = 36.sp
            )
        }
        Slider(
            modifier = Modifier.padding(horizontal = 32.dp),
            value = level,
            onValueChange = onLevelChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.tertiary,
                activeTrackColor = MaterialTheme.colorScheme.tertiary,
//                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            steps = 16,
            valueRange = 0f..17f,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBar(
    statsName: String,
    baseValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
    calculation: (BigDecimal, BigDecimal, Float) -> BigDecimal = { base, per, lv ->
        base + per * BigDecimal(lv.toDouble())
    },
    valueToDisplay: @Composable (BigDecimal) -> String = { value -> "$value" }
) {
    val value = calculation(baseValue, perLevel, level).adaptiveScale()
    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statsName,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(0.36f),
            maxLines = 1
        )
        Text(
            text = valueToDisplay(value), //depend on stats
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(0.2f)
        )
        Slider(
            modifier = Modifier,
            value = value.toFloat(),
            enabled = false,
            onValueChange = { },
            colors = SliderDefaults.colors(
                disabledActiveTrackColor = MaterialTheme.colorScheme.secondary,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.background,
                disabledActiveTickColor = Color.Transparent,
                disabledInactiveTickColor = Color.Transparent
            ),
            valueRange = 0f..maxValue,
            thumb = { Box(Modifier) }
        )
    }
}

@Composable
fun HpBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.hp),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue
    )
}

@Composable
fun HpRegenBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.hp_regen),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun MpBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
    partype: String,
) {
    StatsBar(
        statsName = partype,
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun MpRegenBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
    partype: String,
) {
    StatsBar(
        statsName = stringResource(id = R.string.mp_regen, partype),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun ArmorBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.armor),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun MagicResistanceBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.magic_resist),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun AdBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.attack_damage),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
    )
}

@Composable
fun AsBar(
    minValue: BigDecimal,
    level: Float,
    perLevel: BigDecimal,
    maxValue: Float,
) {
    StatsBar(
        statsName = stringResource(id = R.string.attack_speed),
        baseValue = minValue,
        level = level,
        perLevel = perLevel,
        maxValue = maxValue,
        calculation = { base, per, lv ->
            val ratio = per * BigDecimal(lv.toDouble())
            val perLevelUp = base * ratio / BigDecimal(100)
            val result = base + perLevelUp
            result
        },
    )
}

@Composable
fun StatsBars(level: Float, champion: Champion) {
    Column {
        HpBar(
            minValue = champion.stats.hp,
            level = level,
            perLevel = champion.stats.hpperlevel,
            maxValue = 3500f
        )
        HpRegenBar(
            minValue = champion.stats.hpregen,
            level = level,
            perLevel = champion.stats.hpregenperlevel,
            maxValue = 30f
        )
        MpBar(
            minValue = champion.stats.mp,
            level = level,
            perLevel = champion.stats.mpperlevel,
            maxValue = 2000f,
            partype = champion.partype
        )
        MpRegenBar(
            minValue = champion.stats.mpregen,
            level = level,
            perLevel = champion.stats.mpregenperlevel,
            maxValue = 30f,
            partype = champion.partype
        )
        ArmorBar(
            minValue = champion.stats.armor,
            level = level,
            perLevel = champion.stats.armorperlevel,
            maxValue = 150f
        )
        MagicResistanceBar(
            minValue = champion.stats.spellblock,
            level = level,
            perLevel = champion.stats.spellblockperlevel,
            maxValue = 120f
        )
        AdBar(
            minValue = champion.stats.attackdamage,
            level = level,
            perLevel = champion.stats.attackdamageperlevel,
            maxValue = 160f
        )
        AsBar(
            minValue = champion.stats.attackspeed,
            level = level,
            perLevel = champion.stats.attackspeedperlevel,
            maxValue = 1.5f
        )
    }
}

@Composable
fun ChampionStats(champion: Champion) {
    val scrollState = rememberScrollState()
    var level by rememberSaveable { mutableFloatStateOf(0f) }
    Column(Modifier.verticalScroll(scrollState).semantics { contentDescription = "Champion Stats" }) {
        ChampionLevel(level = level) { level = it }
        StatsBars(level = level, champion = champion)
    }
}

@Preview
@Composable
fun PreviewStats(
    @PreviewParameter(ChampionAndDetailPreviewParameterProvider::class)
    championAndDetail: ChampionAndDetail
) {
    MyApplicationTheme {
        ChampionStats(champion = championAndDetail.champion)
    }
}