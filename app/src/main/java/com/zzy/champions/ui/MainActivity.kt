package com.zzy.champions.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.R
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    private val viewModel: ChampionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                ChampionNavHost(
                    navController = navController,
                    onLinkClick = {
                        goWebUrl(it)
                    },
                    modifier = Modifier
                )
            }
        }
    }
}


fun Activity.goWebUrl(urlLink: String) {
    val link = "${if (!urlLink.contains("http")) "https://"  else ""}$urlLink"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(link)
    try {
        startActivity(i)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_app_for_link, Toast.LENGTH_SHORT).show()
    }
}