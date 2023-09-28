package com.zzy.champions.ui.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

private const val SPLASH_WAIT_TIME: Long = 2000
@Composable
fun LaunchScreen(modifier: Modifier, onTimeout: () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        LaunchedEffect(Unit) {
            delay(SPLASH_WAIT_TIME)
            currentOnTimeout()
        }

        Image(
            modifier = Modifier.align(Alignment.Center).scale(2f),
            painter = painterResource(id = R.drawable.ic_l),
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLaunchScreen() {
    MyApplicationTheme {
        LaunchScreen(modifier = Modifier) {

        }
    }
}
