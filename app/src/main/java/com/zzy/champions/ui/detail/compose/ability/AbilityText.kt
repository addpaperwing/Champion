package com.zzy.champions.ui.detail.compose.ability

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    text: String,
    @ColorRes defaultTextColor: Int = R.color.white,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextColor(ContextCompat.getColor(context, defaultTextColor))
            }

        },
        update = {
            it.text = HtmlCompat.fromHtml(
                text,
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }
    )
}

@Composable
fun AbilityText(
    modifier: Modifier = Modifier,
    type: String,
    name: String,
    description: String
) {
    Column(
        modifier = modifier
            .padding(all = 16.dp)
    ) {
        Text(
            text = type,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
        HtmlText(text = description)
    }
}

@Composable
@Preview
fun PreviewAbilityText() {
    MyApplicationTheme {
        AbilityText(
            type = "Q",
            name = "skill name",
            description = "edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf edsalfkjdslkf"
        )
    }
}