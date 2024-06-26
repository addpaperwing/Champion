package com.zzy.champions.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun TextDialog(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    title: String? = null,
    onPositiveButtonClick: (() -> Unit)? = null,
    onNegativeButtonClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LDialog(
        modifier = modifier.semantics { contentDescription = "Champion build text dialog" },
        onDismissRequest = onDismissRequest,
        onPositiveButtonClick = onPositiveButtonClick,
        onNegativeButtonClick = onNegativeButtonClick
    ) {
        Column(modifier = textModifier.padding(16.dp)) {
            title?.let {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    text = it.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            content()
        }
    }
}

//@Composable
//fun WarningDialog(
//    onDismissRequest: () -> Unit,
//    title: String? = null,
//    onPositiveButtonClick: (() -> Unit)? = null,
//    content: @Composable ColumnScope.() -> Unit,
//) {
//    LDialog(
//        onDismissRequest = onDismissRequest,
//        positiveButtonColor = Color.Red,
//        onPositiveButtonClick = onPositiveButtonClick,
//        onNegativeButtonClick = onDismissRequest
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            title?.let {
//                Text(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 16.dp),
//                    text = it.uppercase(),
//                    color = MaterialTheme.colorScheme.onBackground,
//                    textAlign = TextAlign.Center
//                )
//            }
//            content()
//        }
//    }
//}

@Composable
fun MenuDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    LDialog(
        modifier = modifier.semantics { contentDescription = "Champion build menu dialog" },
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            onEdit?.let {
                MenuItem(
                    modifier = Modifier.semantics { contentDescription = "Edit champion build" },
                    text = stringResource(id = R.string.edit),
                    onClick = it
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            onDelete?.let {
                MenuItem(
                    modifier = Modifier.semantics { contentDescription = "Delete champion build" },
                    text = stringResource(id = R.string.delete), onClick = it)
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
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    name: String? = null,
    desc: String? = null,
    onOkClick: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf(name?:"") }
    var content by remember { mutableStateOf(desc?:"") }
    LDialog(
        modifier = modifier.semantics { contentDescription = "Champion build dialog" },
        onDismissRequest = onDismissRequest,
        onPositiveButtonClick = {
            onOkClick(title, content)
        },
        onNegativeButtonClick = onDismissRequest,
        content = {
            Text(modifier = Modifier
                    .padding(16.dp),
                text = stringResource(id = R.string.add_weblink_build_desc))
            OutlinedTextField(
                modifier = Modifier
                    .padding(16.dp),
                label = { Text(text = stringResource(id = R.string.build_name)) },
                value = title,
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
                label = { Text(text = stringResource(id = R.string.build_weblink)) },
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
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    positiveButtonText: String =  stringResource(id = android.R.string.ok),
    positiveButtonColor: Color = MaterialTheme.colorScheme.tertiary,
    negativeButtonText: String = stringResource(id = android.R.string.cancel),
    negativeButtonColor: Color = MaterialTheme.colorScheme.tertiary,
    onPositiveButtonClick: (() -> Unit)? = null,
    onNegativeButtonClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(modifier = modifier,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    onNegativeButtonClick?.let {
                        TextButton(
                            modifier = Modifier.semantics { contentDescription = "LDialog negative button" },
                            onClick = it) {
                            Text(
                                text = negativeButtonText,
                                color = negativeButtonColor
                            )
                        }
                    }
                    onPositiveButtonClick?.let {
                        TextButton(
                            modifier = Modifier.semantics { contentDescription = "LDialog positive button" },
                            onClick = it) {
                            Text(
                                text = positiveButtonText,
                                color = positiveButtonColor
                            )
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
    }
}

@Preview
@Composable
fun PreviewEditBuildDialog() {
    MyApplicationTheme {
        ChampionBuildDialog(
            onDismissRequest = { /*TODO*/ },
            name =  "OP.GG ARAM",
            desc = "https://www.op.gg/modes/aram/ksante/build?region=kr",
            onOkClick = { name, desc ->

            }
        )
    }
}

@Preview
@Composable
fun PreviewTextDialog() {
    MyApplicationTheme {
        TextDialog(
            onDismissRequest = { /*TODO*/ },
            title = "Confirm",
            onPositiveButtonClick = {

            },
            onNegativeButtonClick = {

            },
            content = {
                Text(text = stringResource(id = R.string.do_you_want_to_delete_this_build))
            })
    }
}