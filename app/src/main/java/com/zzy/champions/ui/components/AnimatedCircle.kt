package com.zzy.champions.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

private const val DIVIDER_LENGTH_IN_DEGREES = 0.6f

@Composable
fun AnimatedCircle(
    data: List<Int>,
    modifier: Modifier = Modifier
) {
    val currentState = remember {
        MutableTransitionState(AnimatedCircleProgress.START)
            .apply { targetState = AnimatedCircleProgress.END }
    }
    val transition = updateTransition(currentState, label = "")
    val angleOffset by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 500,
                durationMillis = 900,
                easing = LinearOutSlowInEasing
            )
        }, label = ""
    ) { progress ->
        if (progress == AnimatedCircleProgress.START) {
            0f
        } else {
            360f
        }
    }

    val colors = chartColors()
    val infos = infos()

    val textMeasurer = rememberTextMeasurer()

    val textStyle = TextStyle(
        fontSize = 12.sp,
        color = Color.Black
    )

    val textToDraw = "Text to draw"

    val textLayoutResult = remember(textToDraw) {
        textMeasurer.measure(textToDraw, textStyle)
    }

    val textHorizontalPadding = textLayoutResult.size.width
    val textVerticalPadding = textLayoutResult.size.height
    val textPadding = textHorizontalPadding.coerceAtLeast(textVerticalPadding)


    Canvas(modifier) {
        val spaceForArc = size.minDimension - textPadding
        val stroke = Stroke(spaceForArc / 16)
        val arcRadius = (spaceForArc - stroke.width) / 2
        var innerRadius = arcRadius
        var count = 0

        val topTextX = size.width/2
        val topTextY = textVerticalPadding/2f

        while (innerRadius > 0) {
            val halfSize = size / 2.0f
            val topLeft = Offset(
                halfSize.width - innerRadius,
                halfSize.height - innerRadius - textVerticalPadding
            )
            val size = Size(innerRadius * 2, innerRadius * 2)

            var startAngle = -90f
            val dividerLength = DIVIDER_LENGTH_IN_DEGREES * arcRadius / innerRadius

            val startAngleOfText = startAngle + (0.2f * 360f)/2
            val SweepAngleOfText = 0.2f * 360f


            (0..4).forEach { pie ->
                val sweep = 0.2f * angleOffset

                val value = ((data[pie] / infos[pie].maxValue.toFloat()) * 10).toInt()
                val factor = 8 - count * 2
                val ringValue = value - factor


                drawArc(
                    color = when {
                        ringValue <= 0 -> colors[0]
                        ringValue == 1 -> colors[2]
                        else -> colors[1]
                    },
                    startAngle = startAngle + dividerLength / 2,
                    sweepAngle = sweep - dividerLength,
                    topLeft = topLeft,
                    size = size,
                    useCenter = false,
                    style = stroke
                )
                startAngle += sweep


                drawText(
                    textMeasurer = textMeasurer,
                    text = textToDraw,
                    style = textStyle,
                    topLeft = Offset(
                        x = topTextX - textLayoutResult.size.width / 2,
                        y = topTextY,
                    )
                )
            }
            innerRadius -= stroke.width * 2f
            count++
        }
    }
}

class Dimension(val name: String, val maxValue: Int)

private fun infos() = listOf<Dimension>(
    Dimension("attack", 10),
    Dimension("defense", 10),
    Dimension("base move speed", maxValue = 355),
    Dimension("base attack range", maxValue = 650),
    Dimension("magic", 10),
)

private fun chartColors() = listOf(
    Color(0xff011f2a), //inactive
    Color(0xff0097ac), //full
    Color(0xff70b5c2), //half
)

private enum class AnimatedCircleProgress { START, END }