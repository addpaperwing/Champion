package com.zzy.champions.ui.detail.compose.ability

import android.text.Html
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zzy.champions.R
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    @ColorRes defaultTextColor: Int = R.color.white,
) {
    val argb = if (color != Color.Unspecified) color.toArgb() else null
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { tv ->
            if (argb != null) tv.setTextColor(argb)
            else tv.setTextColor(tv.context.getColor(defaultTextColor))
            tv.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
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