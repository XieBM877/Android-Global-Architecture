package com.example.globalarch.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private object Palette {
    val Primary = Color(0xFF3F5F90)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD6E3FF)
    val OnPrimaryContainer = Color(0xFF001B3E)
    val Secondary = Color(0xFF565F71)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFDAE2F9)
    val OnSecondaryContainer = Color(0xFF131C2B)
    val Tertiary = Color(0xFF705575)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFFAD8FD)
    val OnTertiaryContainer = Color(0xFF28132E)
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)
    val Background = Color(0xFFF9F9FF)
    val OnBackground = Color(0xFF191C20)
    val Surface = Color(0xFFF9F9FF)
    val OnSurface = Color(0xFF191C20)
    val SurfaceVariant = Color(0xFFE0E2EC)
    val OnSurfaceVariant = Color(0xFF44474E)
    val Outline = Color(0xFF74777F)

    val DarkPrimary = Color(0xFFA9C7FF)
    val DarkOnPrimary = Color(0xFF062F60)
    val DarkPrimaryContainer = Color(0xFF254777)
    val DarkOnPrimaryContainer = Color(0xFFD6E3FF)
    val DarkSecondary = Color(0xFFBEC6DC)
    val DarkOnSecondary = Color(0xFF283141)
    val DarkSecondaryContainer = Color(0xFF3E4759)
    val DarkOnSecondaryContainer = Color(0xFFDAE2F9)
    val DarkTertiary = Color(0xFFDDBCE0)
    val DarkOnTertiary = Color(0xFF3F2844)
    val DarkTertiaryContainer = Color(0xFF573E5C)
    val DarkOnTertiaryContainer = Color(0xFFFAD8FD)
    val DarkError = Color(0xFFFFB4AB)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
    val DarkBackground = Color(0xFF111318)
    val DarkOnBackground = Color(0xFFE2E2E9)
    val DarkSurface = Color(0xFF111318)
    val DarkOnSurface = Color(0xFFE2E2E9)
    val DarkSurfaceVariant = Color(0xFF44474E)
    val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
    val DarkOutline = Color(0xFF8E9099)
}

internal val LightColorScheme = lightColorScheme(
    primary = Palette.Primary,
    onPrimary = Palette.OnPrimary,
    primaryContainer = Palette.PrimaryContainer,
    onPrimaryContainer = Palette.OnPrimaryContainer,
    secondary = Palette.Secondary,
    onSecondary = Palette.OnSecondary,
    secondaryContainer = Palette.SecondaryContainer,
    onSecondaryContainer = Palette.OnSecondaryContainer,
    tertiary = Palette.Tertiary,
    onTertiary = Palette.OnTertiary,
    tertiaryContainer = Palette.TertiaryContainer,
    onTertiaryContainer = Palette.OnTertiaryContainer,
    error = Palette.Error,
    onError = Palette.OnError,
    errorContainer = Palette.ErrorContainer,
    onErrorContainer = Palette.OnErrorContainer,
    background = Palette.Background,
    onBackground = Palette.OnBackground,
    surface = Palette.Surface,
    onSurface = Palette.OnSurface,
    surfaceVariant = Palette.SurfaceVariant,
    onSurfaceVariant = Palette.OnSurfaceVariant,
    outline = Palette.Outline,
)

internal val DarkColorScheme = darkColorScheme(
    primary = Palette.DarkPrimary,
    onPrimary = Palette.DarkOnPrimary,
    primaryContainer = Palette.DarkPrimaryContainer,
    onPrimaryContainer = Palette.DarkOnPrimaryContainer,
    secondary = Palette.DarkSecondary,
    onSecondary = Palette.DarkOnSecondary,
    secondaryContainer = Palette.DarkSecondaryContainer,
    onSecondaryContainer = Palette.DarkOnSecondaryContainer,
    tertiary = Palette.DarkTertiary,
    onTertiary = Palette.DarkOnTertiary,
    tertiaryContainer = Palette.DarkTertiaryContainer,
    onTertiaryContainer = Palette.DarkOnTertiaryContainer,
    error = Palette.DarkError,
    onError = Palette.DarkOnError,
    errorContainer = Palette.DarkErrorContainer,
    onErrorContainer = Palette.DarkOnErrorContainer,
    background = Palette.DarkBackground,
    onBackground = Palette.DarkOnBackground,
    surface = Palette.DarkSurface,
    onSurface = Palette.DarkOnSurface,
    surfaceVariant = Palette.DarkSurfaceVariant,
    onSurfaceVariant = Palette.DarkOnSurfaceVariant,
    outline = Palette.DarkOutline,
)
