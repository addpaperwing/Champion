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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zzy.champions.data.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBottomSheet(
    item: Item,
    version: String,
    onDismiss: () -> Unit,
    onComponentClick: (String) -> Unit,
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
                    model = item.getIconUrl(version),
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

            item.stats.forEach { (key, value) ->
                val label = formatStatKey(key)
                val display = if (value < 1.0) "+${(value * 100).toInt()}%" else "+${value.toInt()}"
                Text(
                    text = "$display $label",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            val descText = item.description
                .replace(Regex("<[^>]+>"), "")
                .trim()
                .ifEmpty { item.plaintext }
            if (descText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = descText, style = MaterialTheme.typography.bodySmall)
            }

            if (item.components.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Builds from:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.components.forEach { componentId ->
                        AsyncImage(
                            model = "https://ddragon.leagueoflegends.com/cdn/$version/img/item/$componentId.png",
                            contentDescription = componentId,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onComponentClick(componentId) },
                        )
                    }
                }
            }

            if (item.upgrades.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Builds into:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.upgrades.forEach { upgradeId ->
                        AsyncImage(
                            model = "https://ddragon.leagueoflegends.com/cdn/$version/img/item/$upgradeId.png",
                            contentDescription = upgradeId,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onComponentClick(upgradeId) },
                        )
                    }
                }
            }
        }
    }
}

private fun formatStatKey(key: String): String =
    key.replace(Regex("^(Flat|Percent)"), "")
        .replace(Regex("Mod$"), "")
        .replace(Regex("([A-Z])"), " $1")
        .trim()
