package com.example.skycast.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    secondary = SunnyYellow,
    tertiary = RainBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = SurfaceLight,
    onSecondary = OnSurfaceLight,
    onTertiary = SurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueLight,
    secondary = SunnyYellow,
    tertiary = RainBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = SurfaceLight,
    onSecondary = OnSurfaceDark,
    onTertiary = SurfaceLight,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
)

@Composable
fun SkyCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
