package io.github.hitchclimber.universaltimers.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Catppuccin Latte (light) ──
private val LatteRosewater = Color(0xFFdc8a78)
private val LatteFlamingo  = Color(0xFFdd7878)
private val LattePink      = Color(0xFFea76cb)
private val LatteMauve     = Color(0xFF8839ef)
private val LatteRed       = Color(0xFFd20f39)
private val LatteMaroon    = Color(0xFFe64553)
private val LattePeach     = Color(0xFFfe640b)
private val LatteYellow    = Color(0xFFdf8e1d)
private val LatteGreen     = Color(0xFF40a02b)
private val LatteTeal      = Color(0xFF179299)
private val LatteSky       = Color(0xFF04a5e5)
private val LatteSapphire  = Color(0xFF209fb5)
private val LatteBlue      = Color(0xFF1e66f5)
private val LatteLavender  = Color(0xFF7287fd)
private val LatteText      = Color(0xFF4c4f69)
private val LatteSubtext1  = Color(0xFF5c5f77)
private val LatteSubtext0  = Color(0xFF6c6f85)
private val LatteOverlay2  = Color(0xFF7c7f93)
private val LatteOverlay1  = Color(0xFF8c8fa1)
private val LatteOverlay0  = Color(0xFF9ca0b0)
private val LatteSurface2  = Color(0xFFacb0be)
private val LatteSurface1  = Color(0xFFbcc0cc)
private val LatteSurface0  = Color(0xFFccd0da)
private val LatteBase      = Color(0xFFeff1f5)
private val LatteMantle    = Color(0xFFe6e9ef)
private val LatteCrust     = Color(0xFFdce0e8)

// ── Catppuccin Mocha (dark) ──
private val MochaRosewater = Color(0xFFf5e0dc)
private val MochaFlamingo  = Color(0xFFf2cdcd)
private val MochaPink      = Color(0xFFf5c2e7)
private val MochaMauve     = Color(0xFFcba6f7)
private val MochaRed       = Color(0xFFf38ba8)
private val MochaMaroon    = Color(0xFFeba0ac)
private val MochaPeach     = Color(0xFFfab387)
private val MochaYellow    = Color(0xFFf9e2af)
private val MochaGreen     = Color(0xFFa6e3a1)
private val MochaTeal      = Color(0xFF94e2d5)
private val MochaSky       = Color(0xFF89dcfe)
private val MochaSapphire  = Color(0xFF74c7ec)
private val MochaBlue      = Color(0xFF89b4fa)
private val MochaLavender  = Color(0xFFb4befe)
private val MochaText      = Color(0xFFcdd6f4)
private val MochaSubtext1  = Color(0xFFbac2de)
private val MochaSubtext0  = Color(0xFFa6adc8)
private val MochaOverlay2  = Color(0xFF9399b2)
private val MochaOverlay1  = Color(0xFF7f849c)
private val MochaOverlay0  = Color(0xFF6c7086)
private val MochaSurface2  = Color(0xFF585b70)
private val MochaSurface1  = Color(0xFF45475a)
private val MochaSurface0  = Color(0xFF313244)
private val MochaBase      = Color(0xFF1e1e2e)
private val MochaMantle    = Color(0xFF181825)
private val MochaCrust     = Color(0xFF11111b)

private val CatppuccinLight = lightColorScheme(
    primary = LatteBlue,
    onPrimary = LatteBase,
    primaryContainer = LatteLavender,
    onPrimaryContainer = LatteText,
    secondary = LatteMauve,
    onSecondary = LatteBase,
    secondaryContainer = LattePink,
    onSecondaryContainer = LatteText,
    tertiary = LatteTeal,
    onTertiary = LatteBase,
    tertiaryContainer = LatteSky,
    onTertiaryContainer = LatteText,
    error = LatteRed,
    onError = LatteBase,
    errorContainer = LatteMaroon,
    onErrorContainer = LatteBase,
    background = LatteBase,
    onBackground = LatteText,
    surface = LatteBase,
    onSurface = LatteText,
    surfaceVariant = LatteMantle,
    onSurfaceVariant = LatteSubtext0,
    outline = LatteOverlay1,
    outlineVariant = LatteSurface1,
    inverseSurface = MochaBase,
    inverseOnSurface = MochaText,
    inversePrimary = MochaBlue,
    surfaceContainerLowest = LatteBase,
    surfaceContainerLow = LatteMantle,
    surfaceContainer = LatteCrust,
    surfaceContainerHigh = LatteSurface0,
    surfaceContainerHighest = LatteSurface1,
)

private val CatppuccinDark = darkColorScheme(
    primary = MochaBlue,
    onPrimary = MochaCrust,
    primaryContainer = MochaLavender,
    onPrimaryContainer = MochaCrust,
    secondary = MochaMauve,
    onSecondary = MochaCrust,
    secondaryContainer = MochaPink,
    onSecondaryContainer = MochaCrust,
    tertiary = MochaTeal,
    onTertiary = MochaCrust,
    tertiaryContainer = MochaSky,
    onTertiaryContainer = MochaCrust,
    error = MochaRed,
    onError = MochaCrust,
    errorContainer = MochaMaroon,
    onErrorContainer = MochaCrust,
    background = MochaBase,
    onBackground = MochaText,
    surface = MochaBase,
    onSurface = MochaText,
    surfaceVariant = MochaMantle,
    onSurfaceVariant = MochaSubtext0,
    outline = MochaOverlay1,
    outlineVariant = MochaSurface1,
    inverseSurface = LatteBase,
    inverseOnSurface = LatteText,
    inversePrimary = LatteBlue,
    surfaceContainerLowest = MochaCrust,
    surfaceContainerLow = MochaMantle,
    surfaceContainer = MochaBase,
    surfaceContainerHigh = MochaSurface0,
    surfaceContainerHighest = MochaSurface1,
)

/**
 * @param darkTheme null = follow system, true = force dark, false = force light
 */
@Composable
fun CatppuccinTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val useDark = darkTheme ?: isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDark) CatppuccinDark else CatppuccinLight,
        content = content,
    )
}
