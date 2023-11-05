package com.zzy.champions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzy.champions.R

@Composable
fun ErrorBar(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.fillMaxWidth().background(Color.Red.copy(alpha = 0.5f)).padding(8.dp),
        text = stringResource(id = R.string.internet_connection_error),
        color = Color.White
    )
}