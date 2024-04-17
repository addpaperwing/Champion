package com.zzy.champions.ui.detail.compose.header

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.zzy.champions.ui.compose.verticalShadowed

@Composable
fun GradientBannerImage(
    modifier: Modifier = Modifier,
    bannerState: BannerState
) {
    AsyncImage(
        model = bannerState.imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1215 / 717f)
            .verticalShadowed()
            .then(modifier),
    )
}

@Composable
fun rememberBannerState(initImageUrl: String = "") = remember {
    BannerState(initImageUrl)
}
class BannerState(initImageUrl: String) {
    var imageUrl by mutableStateOf(initImageUrl)
}