package com.zzy.champions.ui.grid


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.math.BigDecimal

private const val SplashWaitTime: Long = 2000

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.secondary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.league_of_legends),
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.champions).uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier,
            fontSize = 12.sp,
            fontWeight = FontWeight(900)
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {

}

@Composable
fun ChampionsGrid(modifier: Modifier = Modifier, champions: List<Champion>) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(all = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(count = champions.size) { index ->
            ChampionItem(champion = champions[index])
        }
    }
}

@Composable
fun ChampionItem(modifier: Modifier = Modifier, champion: Champion) {
        AsyncImage(
            model = champion.getAvatar(),
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(corner = CornerSize(6.dp)))
                .fillMaxWidth()
                .aspectRatio(1f / 1),
            contentScale = ContentScale.Crop,
        )
}

@Composable
fun LaunchScreen(modifier: Modifier, onTimeout: () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        val currentOnTimeout by rememberUpdatedState(onTimeout)

        LaunchedEffect(Unit) {
            delay(SplashWaitTime)
            currentOnTimeout()
        }

        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(id = R.drawable.league_of_legends),
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

@Preview(showBackground = true, backgroundColor = 0xff111111)
@Composable
fun PreviewChampionGridScreen() {
    val aatrox = Champion(
        "Aatrox",
        266,
        "Aatrox",
        "the Darkin Blade",
        emptyList(),
        "",
        Info(),
        Stats()
    )

    MyApplicationTheme {
        val champions: ArrayList<Champion> = ArrayList()

        for (i in 0..10) {
            champions.add(aatrox)
        }
        Scaffold(
            topBar = {
                TopBar()
            }
        ) { padding ->
            ChampionsGrid(modifier = Modifier.padding(padding), champions = champions)
        }
    }
}