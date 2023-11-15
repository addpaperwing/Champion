package com.zzy.champions.ui.detail.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.ui.components.TextDialog

@Composable
fun Banner(modifier: Modifier = Modifier, imageUrl: String) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1215 / 717f)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent, Color(0xff111111)
                            )
                        )
                    )
                },
        )
    }
}

@Composable
fun GeneralInfo(modifier: Modifier = Modifier, title: String, name: String, tags: List<String>, lore: String) {
    var nameSize by remember { mutableStateOf(IntSize.Zero) }
    var showLoreDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .drawBehind {
                val titleHeight = 10.sp.toPx()
                val distanceToHorizontalEdge = (size.width - nameSize.width) / 2
                val distanceToTopEdge = nameSize.height / 2 + titleHeight
                val strokeWidth = Dp.Hairline.toPx()

                drawLine(
                    color = Color.White,
                    start = Offset(x = distanceToHorizontalEdge, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = size.height),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = size.height),
                    end = Offset(x = size.width, y = size.height),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = size.height),
                    end = Offset(x = size.width, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = distanceToTopEdge),
                    end = Offset(x = size.width - distanceToHorizontalEdge, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp
        )
        Text(
            text = name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .onSizeChanged { size ->
                    nameSize = size
                },
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            lineHeight = 26.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tags.reduce { acc, s -> acc.plus(" · $s") }.uppercase(),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp
            )
            CollapsedText(
                onExpandButtonClick = { /*TODO*/ },
                text = lore,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }

    if (showLoreDialog) {
        TextDialog(
            onDismissRequest = { showLoreDialog = false },
            onPositiveButtonClick = {
                showLoreDialog = false
            },
            content = {
                Text(text = lore, color = MaterialTheme.colorScheme.onBackground)
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

@Composable
fun CollapsedText(
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    expandButtonText: String = stringResource(id = R.string.more),
    onExpandButtonClick: () -> Unit,
    text: String
) {
    var lastCharIndex by remember { mutableIntStateOf(0) }

    val actionTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold
    )
    Text(
        modifier = modifier
            .clickable(onClick = onExpandButtonClick)
            .animateContentSize(),
        text = buildAnnotatedString {
            val adjustText = text.substring(0, lastCharIndex)
                .dropLast(expandButtonText.length)
                .dropLastWhile { Character.isWhitespace(it) || it == '.' }
            append(adjustText)
            withStyle(actionTextStyle) { append(expandButtonText.uppercase()) }
        },
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = maxLines,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        onTextLayout = { layoutResult ->
            if (layoutResult.hasVisualOverflow) {
                lastCharIndex = layoutResult.getLineEnd(maxLines - 1)
            }
        },
    )
}