package com.zzy.champions.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.verticalShadowed(): Modifier = this.drawWithContent {
    drawContent()
    drawRect(
        Brush.verticalGradient(
            listOf(
                Color.Transparent, Color(0xff111111)
            )
        )
    )
}

fun Modifier.verticalDoubleShadowed(): Modifier = this.drawWithContent {
    drawContent()
    drawRect(
        Brush.verticalGradient(
            listOf(
                Color(0xff111111), Color.Transparent, Color(0xff111111)
            )
        )
    )
}