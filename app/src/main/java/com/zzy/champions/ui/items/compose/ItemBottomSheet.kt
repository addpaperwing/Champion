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

// DDragon anomalies: "Flat"-prefixed stats stored as 0–1 ratios despite the name.
// Add new entries here if DDragon introduces another such stat.
private val FLAT_RATIO_STAT_KEYS = setOf("FlatCritChanceMod")
private val REGEX_STAT_PREFIX = Regex("^(Flat|Percent)")
private val REGEX_STAT_SUFFIX = Regex("Mod$")
private val REGEX_ACRONYM_SPLIT = Regex("([A-Z]+)([A-Z][a-z])")
private val REGEX_CAMEL_SPLIT = Regex("([a-z\\d])([A-Z])")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBottomSheet(
    item: Item,
    version: String,
    onDismiss: () -> Unit,
    onComponentClick: (String) -> Unit,
    resolveItem: (String) -> Item?,
    modifier: Modifier = Modifier,
) {
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
                    model = item.getIconUrl(version).takeIf { version.isNotEmpty() },
                    contentDescription = item.name,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${item.gold.total}g  ·  ${item.tags.firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            val statDisplays = remember(item) {
                item.stats.map { (key, value) ->
                    // FlatCritChanceMod is stored as a ratio (0.0–1.0) in DDragon despite the Flat prefix.
                    val isPercent = key.startsWith("Percent") || key in FLAT_RATIO_STAT_KEYS
                    val display = if (isPercent) "+${(value * 100).toInt()}%" else "+${value.toInt()}"
                    "$display ${formatStatKey(key)}"
                }
            }
            statDisplays.forEach { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            val descText = item.description.ifEmpty { item.plaintext }
            if (descText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HtmlText(
                    text = descText,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (item.components.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.builds_from),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                ItemImageRow(ids = item.components, version = version, onItemClick = onComponentClick)
            }

            val visibleUpgrades = remember(item, resolveItem) { item.upgrades.filter { resolveItem(it) != null } }
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

private fun formatStatKey(key: String): String =
    key.replace(REGEX_STAT_PREFIX, "")
        .replace(REGEX_STAT_SUFFIX, "")
        .replace(REGEX_ACRONYM_SPLIT, "$1 $2")
        .replace(REGEX_CAMEL_SPLIT, "$1 $2")
        .trim()
