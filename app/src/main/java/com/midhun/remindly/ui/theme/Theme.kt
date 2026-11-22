package com.midhun.remindly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Every colour slot the app (and Material's pickers, sheets and snackbars) can use is set here,
// so nothing falls back to Material's default purple.
private val LightColors = lightColorScheme(
    primary = Yellow,
    onPrimary = InkOnYellow,
    primaryContainer = LightYellowContainer,
    onPrimaryContainer = LightOnYellowContainer,
    inversePrimary = Yellow,
    secondary = LightMutedText,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightText,
    tertiary = LightMutedText,
    onTertiary = LightSurface,
    tertiaryContainer = LightYellowContainer,
    onTertiaryContainer = LightOnYellowContainer,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMutedText,
    surfaceTint = Yellow,
    inverseSurface = Color(0xFF2B2B26),
    inverseOnSurface = LightBackground,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = YellowSoft,
    onPrimary = InkOnYellow,          // dark text on yellow in BOTH themes
    primaryContainer = DarkYellowContainer,
    onPrimaryContainer = DarkOnYellowContainer,
    inversePrimary = Color(0xFF7A5C00),
    secondary = DarkMutedText,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,
    tertiary = DarkMutedText,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkYellowContainer,
    onTertiaryContainer = DarkOnYellowContainer,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    surfaceTint = YellowSoft,
    inverseSurface = DarkText,
    inverseOnSurface = InkOnYellow,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = DarkBackground,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val RemindlyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** [darkTheme] is decided by the caller (system setting or the user's choice in Settings). */
@Composable
fun RemindlyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = RemindlyShapes,
        content = content
    )
}
