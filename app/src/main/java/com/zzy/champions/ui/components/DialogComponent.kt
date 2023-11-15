package com.zzy.champions.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun TextDialog(
    onDismissRequest: () -> Unit,
    title: @Composable ColumnScope.() -> Unit?,
    content: @Composable ColumnScope.() -> Unit
) {
    LDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            title()
            content()
        }
    }
}

@Composable
fun MenuDialog(
    onDismissRequest: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    LDialog(
        onDismissRequest = onDismissRequest,
        buttons = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            onEdit?.let {
                MenuItem(text = stringResource(id = R.string.edit), onClick = it)
            }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            onDelete?.let {
                MenuItem(text = stringResource(id = R.string.delete), onClick = it)
            }
        }
    }
}

@Composable
fun MenuItem(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ChampionBuildDialog(
    onDismissRequest: () -> Unit,
    initTitle: String,
    initContent: String,
    onOkClick: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf(initTitle) }
    var content by remember { mutableStateOf(initContent) }
    LDialog(
        onDismissRequest = onDismissRequest,
        onPositiveButtonClick = {
            onOkClick(title, content)
        },
        onNegativeButtonClick = onDismissRequest,
        content = {
            OutlinedTextField(
                modifier = Modifier
                    .padding(16.dp),
                label = { Text(text = stringResource(id = R.string.build_name)) },
                value = initTitle,
                onValueChange = {
                    title = it
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
            OutlinedTextField(
                modifier = Modifier
                    .padding(16.dp),
                label = { Text(text = stringResource(id = R.string.build_content)) },
                value = content,
                onValueChange = {
                    content = it
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    )
}

@Composable
fun LDialog(
    onDismissRequest: () -> Unit,
    buttons: Boolean = true,
    onPositiveButtonClick: (() -> Unit)? = null,
    onNegativeButtonClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                content()
                if (buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        onNegativeButtonClick?.let {
                            TextButton(onClick = it) {
                                Text(
                                    text = stringResource(id = android.R.string.cancel),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        onPositiveButtonClick?.let {
                            TextButton(onClick = it) {
                                Text(
                                    text = stringResource(id = android.R.string.ok),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMenuDialog() {
    MyApplicationTheme {
        MenuDialog(onDismissRequest = { /*TODO*/ },
            onEdit = {

            },
            onDelete = {

            })
//        ChampionBuildDialog(
//            onDismissRequest = { /*TODO*/ },
//            initTitle = "OP.GG ARAM",
//            initContent = "https://www.op.gg/modes/aram/ksante/build?region=kr",
//            onOkClick = { title, content ->
//
//            }
//        )
    }
}