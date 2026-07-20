package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.ui.items.ALL_GAME_MODES
import com.zzy.champions.ui.theme.Golden

@Composable
fun FilterIconButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_filter_list),
            contentDescription = stringResource(R.string.filter_items),
            tint = if (isActive) Golden else MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun goldenFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Golden.copy(alpha = 0.25f),
    selectedLabelColor = Golden,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemFilterBottomSheet(
    availableTags: List<String>,
    selectedTags: Set<String>,
    selectedGameModes: Set<String>,
    onTagToggle: (String) -> Unit,
    onGameModeToggle: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
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
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.filter_game_mode),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ALL_GAME_MODES.forEach { mode ->
                    val label = gameModeNameResIds[mode]?.let { stringResource(it) } ?: mode
                    FilterChip(
                        selected = mode in selectedGameModes,
                        onClick = { onGameModeToggle(mode) },
                        label = { Text(label) },
                        colors = goldenFilterChipColors(),
                    )
                }
            }

            Text(
                text = stringResource(R.string.filter_tags),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) },
                        colors = goldenFilterChipColors(),
                    )
                }
            }

            TextButton(
                onClick = onClearAll,
                colors = ButtonDefaults.textButtonColors(contentColor = Golden),
            ) {
                Text(stringResource(R.string.filter_clear_all))
            }
        }
    }
}
