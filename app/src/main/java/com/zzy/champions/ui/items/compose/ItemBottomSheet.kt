package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.itemIconUrl
import com.zzy.champions.ui.detail.compose.ability.HtmlText
import com.zzy.champions.ui.items.ALL_GAME_MODES
import com.zzy.champions.ui.items.ItemGroup
import com.zzy.champions.ui.theme.Golden

// Data Dragon's own description HTML already embeds a complete "<stats>...</stats>" summary
// (e.g. "<attention>80</attention> Health<br>..."), so it's the preferred source of truth for
// stat bonuses here — showing it alongside the structured Item.stats map would just duplicate
// the same numbers under different auto-generated labels. Item.stats is only rendered as a
// fallback for items Data Dragon left with no description text at all (e.g. World Atlas: a real
// 30 HP Pool bonus, but description/plaintext both blank) so that data isn't silently dropped.
//
// When the description's stats block is present but genuinely empty (items with no stat bonuses,
// e.g. consumables), Data Dragon still leaves the trailing "<br><br>" that normally separates it
// from the rest of the text, which otherwise renders as a bare blank gap before the description
// starts.
private val EMPTY_STATS_GAP_REGEX = Regex("<stats>\\s*</stats>(<br\\s*/?>)*", RegexOption.IGNORE_CASE)
private fun stripEmptyStatsGap(description: String): String = description.replace(EMPTY_STATS_GAP_REGEX, "")

// DDragon anomalies: "Flat"-prefixed stats stored as 0–1 ratios despite the name.
// Add new entries here if DDragon introduces another such stat.
private val FLAT_RATIO_STAT_KEYS = setOf("FlatCritChanceMod")
private val REGEX_STAT_PREFIX = Regex("^(Flat|Percent)")
private val REGEX_STAT_SUFFIX = Regex("Mod$")
private val REGEX_ACRONYM_SPLIT = Regex("([A-Z]+)([A-Z][a-z])")
private val REGEX_CAMEL_SPLIT = Regex("([a-z\\d])([A-Z])")

private fun formatStatKey(key: String): String =
    key.replace(REGEX_STAT_PREFIX, "")
        .replace(REGEX_STAT_SUFFIX, "")
        .replace(REGEX_ACRONYM_SPLIT, "$1 $2")
        .replace(REGEX_CAMEL_SPLIT, "$1 $2")
        .trim()

private fun formatStatLine(key: String, value: Double): String {
    val isPercent = key.startsWith("Percent") || key in FLAT_RATIO_STAT_KEYS
    val display = if (isPercent) "+${(value * 100).toInt()}%" else "+${value.toInt()}"
    return "$display ${formatStatKey(key)}"
}

private class VariantContent(val descText: String, val statLines: List<String>) {
    val isEmpty: Boolean = descText.isEmpty() && statLines.isEmpty()
}

private fun variantContent(variant: Item): VariantContent {
    val descText = stripEmptyStatsGap(variant.description.ifEmpty { variant.plaintext })
    val statLines = if (descText.isNotEmpty()) emptyList() else variant.stats.map { (key, value) -> formatStatLine(key, value) }
    return VariantContent(descText, statLines)
}

private fun contentGroups(distinctVariants: List<Item>, contents: List<VariantContent>): List<Pair<VariantContent, List<Item>>> =
    distinctVariants.indices
        .groupBy { contents[it].descText to contents[it].statLines }
        .values
        .map { idxGroup -> contents[idxGroup.first()] to idxGroup.map { distinctVariants[it] } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBottomSheet(
    item: ItemGroup,
    version: String,
    onDismiss: () -> Unit,
    onComponentClick: (String) -> Unit,
    resolveItem: (String) -> ItemGroup?,
    modifier: Modifier = Modifier,
) {
    val primary = item.primary
    // Variants only need breaking out by mode when they actually differ; two catalog entries
    // that happen to share a name/icon but carry identical gold+description are the same item
    // in every way that matters here and should render exactly like a single-variant item.
    val distinctVariants = remember(item) { item.variants.distinctBy { it.gold to it.description } }
    val variantContents = remember(distinctVariants) { distinctVariants.map { variantContent(it) } }
    // Variants that differ only in gold (e.g. mode-specific pricing) but share the same stats
    // and description render as one body section with a combined mode+gold header instead of
    // repeating the same content once per mode.
    val groups = remember(distinctVariants, variantContents) { contentGroups(distinctVariants, variantContents) }
    val visibleUpgrades = remember(item, resolveItem) { primary.upgrades.filter { resolveItem(it) != null } }

    // Multiple distinct variants always have something worth showing (at minimum, their gold
    // differs — that's why they're distinct); a single variant is only worth a body section if
    // it actually has stats or a description. Hides the divider/section entirely rather than
    // leaving a gap in front of nothing (e.g. a component with no text and no stats at all).
    val hasBodyContent = distinctVariants.size > 1 || variantContents.any { !it.isEmpty }
    val hasAnythingBelowHeader = hasBodyContent || primary.components.isNotEmpty() || visibleUpgrades.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = primary.getIconUrl(version).takeIf { version.isNotEmpty() },
                    contentDescription = primary.name,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = primary.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (distinctVariants.size <= 1) {
                            "${primary.gold.total}g  ·  ${primary.tags.firstOrNull() ?: ""}"
                        } else {
                            primary.tags.firstOrNull() ?: ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (hasAnythingBelowHeader) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.testTag("item_detail_divider"))
                Spacer(Modifier.height(12.dp))

                if (hasBodyContent) {
                    if (distinctVariants.size <= 1) {
                        ItemVariantBody(content = variantContents.first(), variants = listOf(primary), showModeLabel = false)
                    } else {
                        groups.forEachIndexed { index, (content, variants) ->
                            if (index > 0) Spacer(Modifier.height(16.dp))
                            ItemVariantBody(content = content, variants = variants, showModeLabel = true)
                        }
                    }
                }

                if (primary.components.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.builds_from),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    ItemImageRow(ids = primary.components, version = version, onItemClick = onComponentClick)
                }

                if (visibleUpgrades.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.builds_into),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    ItemImageRow(ids = visibleUpgrades, version = version, onItemClick = onComponentClick)
                }
            }
        }
    }
}

@Composable
private fun ItemVariantBody(content: VariantContent, variants: List<Item>, showModeLabel: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (showModeLabel) {
            val header = variants.map { variant ->
                val modeLabel = ALL_GAME_MODES
                    .filter { variant.maps[it] == true }
                    .mapNotNull { gameModeNameResIds[it] }
                    .map { stringResource(it) }
                    .joinToString(" / ")
                "$modeLabel  ·  ${variant.gold.total}g"
            }.joinToString(" / ")
            Text(
                text = header,
                style = MaterialTheme.typography.labelMedium,
                color = Golden,
            )
            if (!content.isEmpty) Spacer(Modifier.height(4.dp))
        }

        content.statLines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        if (content.descText.isNotEmpty()) {
            HtmlText(
                text = content.descText,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ItemImageRow(
    ids: List<String>,
    version: String,
    onItemClick: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ids.forEach { id ->
            AsyncImage(
                model = itemIconUrl(version, id).takeIf { version.isNotEmpty() },
                contentDescription = id,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onItemClick(id) },
            )
        }
    }
}
