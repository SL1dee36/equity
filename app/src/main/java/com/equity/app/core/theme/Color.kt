package com.equity.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian Dark Palette
val ObsidianBlack = Color(0xFF121212)
val ObsidianSurface = Color(0xFF181A1B)
val ObsidianCard = Color(0xFF1F2224)
val ObsidianCardElevated = Color(0xFF262A2D)
val ObsidianBorder = Color(0xFF2D3236)
val ObsidianDivider = Color(0xFF2A2E32)

// Electric Mint / Emerald Wealth Palette
val ElectricMint = Color(0xFF00E676)
val ElectricMintDark = Color(0xFF00B359)
val ElectricMintSoft = Color(0xFF69F0AE)
val ElectricMintSubtle = Color(0x1A00E676)
val ElectricMintGlow = Color(0x3300E676)

// Platinum & Muted Silver Palette
val PlatinumWhite = Color(0xFFF5F7FA)
val PlatinumText = Color(0xFFECEFF1)
val MutedSilver = Color(0xFFB0BEC5)
val DarkSilver = Color(0xFF78909C)
val SubduedSilver = Color(0xFF546E7A)

// Functional & Feedback Accents
val ExpenseRed = Color(0xFFFF5252)
val ExpenseRedSubtle = Color(0x26FF5252)
val WarningAmber = Color(0xFFFFB300)
val WarningAmberSubtle = Color(0x26FFB300)
val InfoCyan = Color(0xFF00B0FF)
val PurpleAccent = Color(0xFF7C4DFF)

// Gradients
val EquityCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF232A26),
        Color(0xFF181C1A)
    )
)

val MintGlowGradient = Brush.horizontalGradient(
    colors = listOf(
        ElectricMint,
        Color(0xFF00B0FF)
    )
)

val DarkMeshGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF1F2D24),
        Color(0xFF121212)
    )
)
