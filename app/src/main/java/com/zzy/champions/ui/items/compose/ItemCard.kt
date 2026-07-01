package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zzy.champions.data.model.Item
import com.zzy.champions.ui.theme.DarkLight
import com.zzy.champions.ui.theme.Golden

@Composable
fun ItemCard(
    item: Item,
    version: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .border(Dp.Hairline, Golden.copy(alpha = 0.5f), itemCutCornerShape)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth()
                .background(DarkLight, itemCutCornerShape)
                .clip(itemCutCornerShape)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = item.getIconUrl(version).takeIf { version.isNotEmpty() },
                contentDescription = item.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )
            Text(
                text = item.name,
                fontSize = 7.sp,
                fontWeight = FontWeight(600),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
            )
            Text(
                text = "${item.gold.total}g",
                fontSize = 7.sp,
                color = Golden,
                maxLines = 1,
            )
        }
    }
}
