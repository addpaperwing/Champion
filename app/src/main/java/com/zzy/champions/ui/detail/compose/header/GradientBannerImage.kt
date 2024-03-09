package com.zzy.champions.ui.detail.compose.header

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.zzy.champions.ui.compose.verticalShadowed

@Composable
fun GradientBannerImage(
    modifier: Modifier = Modifier,
    imageUrl: String
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1215 / 717f)
            .verticalShadowed()
            .then(modifier),
    )
}