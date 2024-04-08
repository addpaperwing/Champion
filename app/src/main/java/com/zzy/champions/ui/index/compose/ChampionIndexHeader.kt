package com.zzy.champions.ui.index.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R

@Composable
fun Header(
    modifier: Modifier = Modifier,
    version: String,
    onSettingClick: () -> Unit,
) {
    Box(modifier = modifier
        .padding(top = 16.dp)
        .fillMaxWidth()) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.choose_your).uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 10.sp
            )
            Text(
                text = stringResource(id = R.string.champion).uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 36.sp,
                fontWeight = FontWeight(900),
                fontStyle = FontStyle.Italic
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.choose_your_champion_desc),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                textAlign = TextAlign.End,
                text = stringResource(id = R.string.v_, version),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 8.sp,
            )
        }
//        IconButton(
//            modifier = Modifier.align(Alignment.TopEnd),
//            onClick = onSettingClick
//        ) {
//            Icon(imageVector = Icons.Default.Settings, contentDescription = "settings", tint = MaterialTheme.colorScheme.tertiary)
//        }
    }
}