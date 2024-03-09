package com.zzy.champions.ui.detail.compose.header

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.ui.compose.TextDialog

@Composable
fun CollapsedText(
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    expandButtonText: String = stringResource(id = R.string.more),
    onExpandButtonClick: () -> Unit = {},
    text: String
) {
    var lastCharIndex by remember { mutableIntStateOf(0) }
    var clickable by remember { mutableStateOf(false) }
    val actionTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold
    )

    var showLoreDialog by remember { mutableStateOf(false) }

    Text(
        modifier = modifier
            .clickable(onClick = {
                showLoreDialog = true
                onExpandButtonClick()
            }),
        text = buildAnnotatedString {
            if (clickable) {
                val adjustText = text.substring(0, lastCharIndex)
                    .dropLast(expandButtonText.length)
                    .dropLastWhile { Character.isWhitespace(it) || it == '.' }
                append(adjustText)
                withStyle(actionTextStyle) { append(expandButtonText.uppercase()) }
            } else {
                append(text)
            }
        },
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = maxLines,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        onTextLayout = { layoutResult ->
            if (layoutResult.hasVisualOverflow) {
                clickable = true
                lastCharIndex = layoutResult.getLineEnd(maxLines - 1)
            }
        },
    )

    if (showLoreDialog) {
        TextDialog(
            onDismissRequest = { showLoreDialog = false },
            onPositiveButtonClick = {
                showLoreDialog = false
            },
            content = {
                Text(text = text, color = MaterialTheme.colorScheme.onBackground)
            })
    }
}

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    maxLinesWhenCollapsed: Int = 3,
    expandButtonText: String = stringResource(id = R.string.more),
    collapsedButtonText: String = stringResource(id = R.string.less),
    text: String
) {
    var expanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableIntStateOf(0) }

    val actionTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold
    )
    Text(
        modifier = modifier
            .clickable {
                expanded = !expanded
            }
            .animateContentSize(),
        text = buildAnnotatedString {
            if (clickable) {
                if (expanded) {
                    append(text)
                    withStyle(actionTextStyle) { append(collapsedButtonText.uppercase()) }
                } else {
                    val adjustText = text.substring(0, lastCharIndex)
                        .dropLast(expandButtonText.length)
                        .dropLastWhile { Character.isWhitespace(it) || it == '.' }
                    append(adjustText)
                    withStyle(actionTextStyle) { append(expandButtonText.uppercase()) }
                }
            } else {
                append(text)
            }
        },
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = if (expanded) Int.MAX_VALUE else maxLinesWhenCollapsed,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        onTextLayout = { layoutResult ->
            if (!expanded && layoutResult.hasVisualOverflow) {
                clickable = true
                lastCharIndex = layoutResult.getLineEnd(maxLinesWhenCollapsed - 1)
            }
        },
    )
}