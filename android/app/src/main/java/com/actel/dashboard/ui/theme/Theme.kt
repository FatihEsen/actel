package com.actel.dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonBlue,
    tertiary = NeonGreen,
    background = BgDark,
    surface = BgCard,
    onPrimary = BgDark,
    onSecondary = TextPrimary,
    onTertiary = BgDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ActelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
