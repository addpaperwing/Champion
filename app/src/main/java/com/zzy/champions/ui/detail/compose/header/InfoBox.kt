package com.zzy.champions.ui.detail.compose.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoBox(modifier: Modifier = Modifier, title: String, name: String, tags: List<String>, lore: String) {
    var nameSize by remember { mutableStateOf(IntSize.Zero) }
    
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
                text = lore,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}



