package com.zzy.champions.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        var showWaitText by remember { mutableStateOf(false) }
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        LaunchedEffect(Unit) {
            delay(SPLASH_WAIT_TIME)
            currentOnTimeout()
            showWaitText = true
        }

        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(2f),
            painter = painterResource(id = R.drawable.ic_l),
            contentDescription = null
        )

        if (showWaitText) {
            Text(
                modifier = Modifier.padding(32.dp).align(Alignment.BottomCenter),
                text = stringResource(id = R.string.it_might_take_a_bit_while_for_the_first_load),
                color = Color.White,
                fontSize = 10.sp
            )
        }
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
