package com.zzy.champions.ui.detail.compose.skin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.ui.compose.verticalShadowed
import com.zzy.champions.ui.theme.Golden

@Composable
fun SkinItem(modifier: Modifier = Modifier,
             championId: String,
             skinNumber: SkinNumber,
             isSelected: Boolean,
             onClick: (SkinNumber) -> Unit)
{
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable {
                onClick(skinNumber)
            }
        ,
        border = if (isSelected) BorderStroke(width = Dp.Hairline, color = Golden) else null,
    ) {
        Box(modifier = Modifier.height(128.dp)) {
            AsyncImage(
                model = skinNumber.getSplash(championId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalShadowed(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
            Text(
                text = skinNumber.name,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                maxLines = 1
            )
        }
    }
}