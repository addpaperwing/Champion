package com.zzy.champions.ui.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.theme.Dark
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun BlurBanner(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.splash_aatrox_0),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1215 / 717f)

        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .aspectRatio(1215 / 717f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent, MaterialTheme.colorScheme.primary
                        )
                    )
                )
        )
    }
}

@Composable
fun GeneralInfo(modifier: Modifier = Modifier, champion: Champion) {
//    Column(modifier = modifier) {
//        Text(text = )
//    }
}



@Preview(showBackground = true, backgroundColor = 0xff111111)
@Composable
fun PreviewDetailScreen() {
    MyApplicationTheme {
        BlurBanner()
    }
}
