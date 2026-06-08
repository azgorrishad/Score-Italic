package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.PreferencesManager

private val ModernLightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onBackground = TextDarkOnLight,
    onSurface = TextDarkOnLight,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569) // Slate 600
)

private val DarkColorScheme = darkColorScheme(
    primary = VibrantBlueDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = TextLightOnDark,
    onSurface = TextLightOnDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val PureBlackColorScheme = darkColorScheme(
    primary = NeonCyanAmoled,
    background = BackgroundAmoled,
    surface = SurfaceAmoled,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFAAAAAA)
)

private val GoldEliteColorScheme = darkColorScheme(
    primary = LuxuryGold,
    background = RichCharcoalBg,
    surface = StoneSurface,
    onPrimary = RichCharcoalBg,
    onBackground = IvoryText,
    onSurface = IvoryText,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFBBBBBB)
)

private val RoyalBlueColorScheme = darkColorScheme(
    primary = RoyalBlue,
    background = RoyalBlueBg,
    surface = RoyalBlueSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2B4180),
    onSurfaceVariant = Color(0xFFD0D8F0)
)

private val EmeraldGreenColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    background = EmeraldBg,
    surface = EmeraldSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF0D7357),
    onSurfaceVariant = Color(0xFFC7F0E6)
)

private val CrimsonRedColorScheme = darkColorScheme(
    primary = CrimsonRed,
    background = CrimsonBg,
    surface = CrimsonSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF8B1A24),
    onSurfaceVariant = Color(0xFFF0C7CC)
)

private val PurpleNeonColorScheme = darkColorScheme(
    primary = PurpleNeon,
    background = PurpleBg,
    surface = PurpleSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF5A1C8C),
    onSurfaceVariant = Color(0xFFE4C7F0)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberYellow,
    background = CyberBg,
    surface = CyberSurface,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFFD1D5DB)
)

private val VintageCardTableColorScheme = darkColorScheme(
    primary = FeltGreen,
    background = WoodBrown,
    surface = CardCream,
    onPrimary = Color.White,
    onBackground = VintageText,
    onSurface = WoodBrown,
    surfaceVariant = Color(0xFFE5DECB),
    onSurfaceVariant = WoodBrown
)

@Composable
fun MyApplicationTheme(
    themeType: PreferencesManager.AppTheme = PreferencesManager.AppTheme.MODERN_LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        PreferencesManager.AppTheme.MODERN_LIGHT -> ModernLightColorScheme
        PreferencesManager.AppTheme.DARK -> DarkColorScheme
        PreferencesManager.AppTheme.PURE_BLACK -> PureBlackColorScheme
        PreferencesManager.AppTheme.GOLD_ELITE -> GoldEliteColorScheme
        PreferencesManager.AppTheme.ROYAL_BLUE -> RoyalBlueColorScheme
        PreferencesManager.AppTheme.EMERALD_GREEN -> EmeraldGreenColorScheme
        PreferencesManager.AppTheme.CRIMSON_RED -> CrimsonRedColorScheme
        PreferencesManager.AppTheme.PURPLE_NEON -> PurpleNeonColorScheme
        PreferencesManager.AppTheme.CYBERPUNK -> CyberpunkColorScheme
        PreferencesManager.AppTheme.VINTAGE_CARD_TABLE -> VintageCardTableColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

