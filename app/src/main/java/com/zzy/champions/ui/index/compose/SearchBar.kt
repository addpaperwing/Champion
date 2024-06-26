package com.zzy.champions.ui.index.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zzy.champions.ui.theme.ASSASSIN
import com.zzy.champions.ui.theme.MAGE
import com.zzy.champions.ui.theme.MARKSMAN
import com.zzy.champions.ui.theme.MyApplicationTheme

internal val PREDICTION_ITEM_HEIGHT = 56.dp

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChanged: (String) -> Unit,
    onClearText: (() -> Unit)? = null,
    onDone: (String) -> Unit
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp))
                .border(
                    Dp.Hairline,
                    Color.White,
                    shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
                ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            trailingIcon = {
                if (onClearText != null && text.isNotBlank()) {
                    IconButton(onClick = onClearText) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.tertiary,
                    backgroundColor =  MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                ),

            ),
            value = text,
            onValueChange = { newValue ->
                onTextChanged(newValue)
            },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = {
                onDone(text)
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            )
        )
    }
}

@Composable
fun <T> PredictionItem(modifier: Modifier = Modifier, item: T, onDisplay: (T) -> String = {it.toString()}, onClick: (T) -> Unit) {
    Row(
        modifier = modifier
            .height(PREDICTION_ITEM_HEIGHT)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(item)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = onDisplay(item).uppercase(),
            color = MaterialTheme.colorScheme.onSurface
            )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> PredictionSearchBar(
    modifier: Modifier,
    predictions: List<T>,
    onTextChanged: (String) -> Unit,
    onDoneActionClick: (String) -> Unit,
    onPredictionClick: (T) -> Unit,
    getDisplayText: (T) -> String = { it.toString() }
) {
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .heightIn(max = PREDICTION_ITEM_HEIGHT * 5.5f),
    ) {
        stickyHeader {
            SearchTextField(
                text = text,
                onTextChanged = {
                    text = it
                    onTextChanged(text)
                },
                onDone = {
                    keyboardController?.hide()
                    onDoneActionClick(it)
                }
            )
        }

        if (predictions.isNotEmpty()) {
            items(predictions) {
                PredictionItem(modifier = Modifier, it, onDisplay = getDisplayText) { prediction ->
                    keyboardController?.hide()
                    onPredictionClick(prediction)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPrediction() {
    MyApplicationTheme {
        Column {
            PredictionSearchBar(
                modifier = Modifier,
                predictions = listOf(
                    ASSASSIN,
                    MARKSMAN,
                    MAGE,
                    "Aatrox",
                    ASSASSIN,
                    MARKSMAN,
                    MAGE,
                    "Aatrox"
                ),
                onTextChanged = {

                },
                onDoneActionClick = {

                },
                onPredictionClick = {

                }
            )
        }
    }
}