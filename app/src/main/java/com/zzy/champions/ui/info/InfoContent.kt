package com.zzy.champions.ui.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.Info
import com.zzy.champions.ui.components.AnimatedCircle
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.math.BigDecimal

@Composable
fun InfoPie(info: Info, bMoveSpeed: BigDecimal, bAttackRange: BigDecimal) {
    Box(Modifier.padding(16.dp)) {
        AnimatedCircle(
            listOf(
                9, 7, 5, 3, 1
            ),
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
@Preview
fun PreviewPie() {
    MyApplicationTheme {
        AnimatedCircle(
            listOf(
                9, 7, 5, 320, 125
            ),
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )


    }
}