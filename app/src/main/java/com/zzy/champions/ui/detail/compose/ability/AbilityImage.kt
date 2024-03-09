package com.zzy.champions.ui.detail.compose.ability

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AbilityImage(
    modifier: Modifier = Modifier,
    model: String,
    contentDescription: String?,
    activeColor: Color,
    inactiveColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedOffsetDp: Dp by animateDpAsState(
        targetValue = if (isSelected) 0.dp else 16.dp,
        label = "image offset"
    )
    val animatedScale: Float by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "circle scale"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "dot color"
    )

    val imageRaisedHeight = 16
    val imageSize = 48
    val composableHeight = imageRaisedHeight * 2 + imageSize

    val strokeWidth = 1
    val circleSize = 10.dp
    val innerCircle = 6.dp

    Column(modifier = modifier
        .clickable(onClick = onClick)
        .height(composableHeight.dp)) {
        Box(
            modifier = Modifier
                .offset(0.dp, animatedOffsetDp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(imageSize.dp)
                    .padding(3.dp),
                model = model,
                contentDescription = contentDescription
            )
            if (isSelected) {
                Box(
                    Modifier
                        .size(imageSize.dp)
                        .border(
                            border = BorderStroke(
                                Dp.Hairline,
                                Brush.horizontalGradient(listOf(activeColor, activeColor))
                            ),
                            shape = CutCornerShape(topEnd = (imageSize / 4).dp)
                        )
                )
            }
        }
        Canvas(modifier = Modifier.padding(top = imageRaisedHeight.dp)) {
            val outlineStroke = Stroke(strokeWidth.dp.toPx())

            val x = imageSize.dp.toPx() / 2
            val y = imageRaisedHeight.dp.toPx() / 2
            val circleCenter = Offset(x, y)
            val innerRadius = innerCircle.toPx() / 2
            val radius = (circleSize.toPx() * animatedScale) / 2

            if (isSelected) {
                drawLine(
                    color = activeColor,
                    start = Offset(x, y - radius),
                    end = Offset(x, 0f - y * 2),
                    strokeWidth = Dp.Hairline.toPx()
                )
            }

            drawCircle(
                color = dotColor,
                center = circleCenter,
                radius = innerRadius
            )

            drawCircle(
                color = activeColor,
                style = outlineStroke,
                center = circleCenter,
                radius = radius
            )
        }
    }
}