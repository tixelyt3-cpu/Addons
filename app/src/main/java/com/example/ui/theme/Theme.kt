package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinecraftDarkColorScheme = darkColorScheme(
    primary = CreeperGreen,
    secondary = GoldIngot,
    tertiary = DiamondCyan,
    background = ObsidianDark,
    surface = DeepslateSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = IronGraySurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = CobblestoneBorder
)

// We keep a fallback light colorscheme, but default to dark because MC Addons applications
// look extraordinarily better and realistic in Dark Obsidian mode!
private val MinecraftLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Deep Forest Green
    secondary = Color(0xFFF57C00), // Pumpkin Orange
    tertiary = Color(0xFF0097A7), // Cyan
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFBDBDBD)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // For general modern game looks, let's force dark theme to stay in the thematic Minecraft mood!
    forceDark: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (forceDark || darkTheme) {
        MinecraftDarkColorScheme
    } else {
        MinecraftLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
