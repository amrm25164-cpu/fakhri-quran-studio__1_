package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldPrimary,
    onPrimary = IslamicEmeraldDark,
    primaryContainer = IslamicGoldContainer,
    onPrimaryContainer = IslamicGoldLight,
    secondary = IslamicEmeraldAccent,
    onSecondary = IslamicCream,
    secondaryContainer = IslamicEmeraldMedium,
    onSecondaryContainer = IslamicCream,
    tertiary = IslamicGoldLight,
    onTertiary = SurfaceDark,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceContainerDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = IslamicEmeraldAccent.copy(alpha = 0.5f),
    outlineVariant = SurfaceContainerHighestDark,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning dark luxury emerald theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SurfaceDark.toArgb()
                window.navigationBarColor = SurfaceDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
