package com.zzy.champions.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = UiBlue,
    onPrimary = Color.White,

    secondary = EssenceBlue,
    onSecondary = Color.Gray,

    tertiary = Golden,
    onTertiary = Color.Black,

    background = Dark,
    onBackground = Color.White,

    surface = DarkLight,
    onSurface = Color.LightGray,
)

//private val LightColorScheme = lightColorScheme(
//    primary = Color.White,
//    onPrimary = Dark,
//
//    secondary = UiBlue,
//    onSecondary = Color.Gray,
//
//    tertiary = DarkLight,
//    onTertiary = Golden,
//
//    onSurface = EssenceBlue,
//    background = Dark

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
//)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme

//        when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = Color.Transparent.toArgb()
//            window.navigationBarColor = Color.Transparent.toArgb()
//
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
////                window.setDecorFitsSystemWindows(false)
////            }
//            WindowCompat.setDecorFitsSystemWindows(window, false)
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}