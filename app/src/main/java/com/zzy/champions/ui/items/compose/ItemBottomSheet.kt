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
// (e.g. "<attention>80</attention> Health<br>..."), so it's the single source of truth for
// stat bonuses here — no separate structured display is derived from Item.stats, which would
// just duplicate the same numbers under different auto-generated labels.
//
// When that block is genuinely empty (items with no stat bonuses, e.g. consumables), Data
// Dragon still leaves the trailing "<br><br>" that normally separates it from the rest of the
// text, which otherwise renders as a bare blank gap before the description starts.
private val EMPTY_STATS_GAP_REGEX = Regex("<stats>\\s*</stats>(<br\\s*/?>)*", RegexOption.IGNORE_CASE)
private fun stripEmptyStatsGap(description: String): String = description.replace(EMPTY_STATS_GAP_REGEX, "")

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

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            if (distinctVariants.size <= 1) {
                ItemVariantBody(variant = primary, showModeLabel = false)
            } else {
                distinctVariants.forEachIndexed { index, variant ->
                    if (index > 0) Spacer(Modifier.height(16.dp))
                    ItemVariantBody(variant = variant, showModeLabel = true)
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

            val visibleUpgrades = remember(item, resolveItem) { primary.upgrades.filter { resolveItem(it) != null } }
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

@Composable
private fun ItemVariantBody(variant: Item, showModeLabel: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (showModeLabel) {
            val modeLabel = ALL_GAME_MODES
                .filter { variant.maps[it] == true }
                .mapNotNull { gameModeNameResIds[it] }
                .map { stringResource(it) }
                .joinToString(" / ")
            Text(
                text = "$modeLabel  ·  ${variant.gold.total}g",
                style = MaterialTheme.typography.labelMedium,
                color = Golden,
            )
            Spacer(Modifier.height(4.dp))
        }

        val descText = stripEmptyStatsGap(variant.description.ifEmpty { variant.plaintext })
        if (descText.isNotEmpty()) {
            HtmlText(
                text = descText,
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
