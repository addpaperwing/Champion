package com.zzy.champions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.SettingSelectable
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun SettingBottomSheetItem(
    modifier: Modifier = Modifier,
    content: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .clickable(onClick = onClick),
        text = content,
        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SelectableItem(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Row(modifier = modifier
        .background(MaterialTheme.colorScheme.surface)
        .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondary,
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableBottomMenu(
    modifier: Modifier = Modifier,
    list: List<SettingSelectable>,
    onItemSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val versions = list.toMutableStateList()
    var selection by remember { mutableStateOf("") }

    LaunchedEffect(key1 = versions) {
        versions.forEach {
            if (it.isSelected) selection = it.name
        }
    }

    SelectableItem(modifier = modifier, text = selection) {
        showBottomSheet = true
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(versions) { item ->
                    SettingBottomSheetItem(content = item.name, isSelected = item.isSelected) {
                        versions.forEach {
                            it.isSelected = it.name == item.name
                        }
                        selection = item.name

                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                            onItemSelected(item.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewVDD() {
    MyApplicationTheme {
        Scaffold { padding ->
            SelectableBottomMenu(
                modifier = Modifier.padding(padding),
                list = listOf(
                    SettingSelectable("1.13.1", false),
                    SettingSelectable("1.13.2", false),
                    SettingSelectable("1.13.3", true),
                    SettingSelectable("1.13.4", false),
                    SettingSelectable("1.13.5", false),
                    SettingSelectable("1.13.6", false),
                ),
                onItemSelected = {

                }
            )
        }
    }
}