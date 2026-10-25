package com.equity.app.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricMint,
    onPrimary = ObsidianBlack,
    primaryContainer = ElectricMintDark,
    onPrimaryContainer = PlatinumWhite,
    secondary = MutedSilver,
    onSecondary = ObsidianBlack,
    secondaryContainer = ObsidianCardElevated,
    onSecondaryContainer = PlatinumWhite,
    tertiary = InfoCyan,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PlatinumWhite,
    surface = ObsidianSurface,
    onSurface = PlatinumWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = MutedSilver,
    outline = ObsidianBorder,
    outlineVariant = ObsidianDivider,
    error = ExpenseRed,
    onError = PlatinumWhite
)

@Composable
fun EquityTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EquityTypography,
        content = content
    )
}
