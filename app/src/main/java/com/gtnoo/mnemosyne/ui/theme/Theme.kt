package com.gtnoo.mnemosyne.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo20,
    primaryContainer = Indigo40,
    onPrimaryContainer = Indigo90,
    secondary = Amber80,
    onSecondary = Amber40,
    secondaryContainer = Color(0xFF5A3900),
    onSecondaryContainer = Amber90,
    tertiary = Rose80,
    onTertiary = Rose40,
    tertiaryContainer = Color(0xFF5E1030),
    onTertiaryContainer = Rose90,
    background = NeutralGray10,
    onBackground = NeutralGray90,
    surface = NeutralGray20,
    onSurface = NeutralGray90,
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    error = ErrorRed80,
    onError = ErrorRed40
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Amber40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Amber90,
    onSecondaryContainer = Color(0xFF2C1600),
    tertiary = Rose40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Rose90,
    onTertiaryContainer = Color(0xFF3B0017),
    background = Indigo99,
    onBackground = NeutralGray10,
    surface = NeutralGray99,
    onSurface = NeutralGray10,
    surfaceVariant = NeutralGray95,
    onSurfaceVariant = Color(0xFF46464F),
    error = ErrorRed40,
    onError = Color(0xFFFFFFFF),
    errorContainer = ErrorRed90,
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MnemosyneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MnemosyneTypography,
        content = content
    )
}
