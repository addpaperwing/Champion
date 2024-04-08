package com.zzy.champions.ui.detail.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun LoadingAndErrorScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    onReloadClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp).semantics { contentDescription = "Loading indicator" },
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        if (isError) {
            Text(
                modifier = Modifier.semantics { contentDescription = "Error message" },
                text = stringResource(id = R.string.error_when_loading))
            Button(
                modifier = Modifier.semantics { contentDescription = "Reload button" },
                onClick = onReloadClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
            ) {
                Text(text = stringResource(id = R.string.reload))
            }
        }
    }
}

@Preview
@Composable
fun PreviewLoading() {
    MyApplicationTheme {


        LoadingAndErrorScreen(isLoading = true, isError = false) {

        }
    }
}

@Preview
@Composable
fun PreviewError() {
    MyApplicationTheme {


        LoadingAndErrorScreen(isLoading = false, isError = true) {

        }
    }
}