package com.workout.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Available app themes
 */
enum class AppTheme(val displayName: String) {
    CYAN("Cyan (Default)"),
    PURPLE("Purple"),
    ORANGE("Orange"),
    GREEN("Green"),
    RED("Red"),
    BLUE("Blue"),
    PINK("Pink")
}

// Cyan Theme (Default)
private val CyanDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF70F7DC),
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFF4A2D6E),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = Color(0xFF5C3D00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// Purple Theme
private val PurpleDarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = PurpleAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Orange Theme
private val OrangeDarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = OrangeOnPrimaryContainer,
    secondary = OrangeAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Green Theme
private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = GreenAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Red Theme
private val RedDarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = RedPrimaryContainer,
    onPrimaryContainer = RedOnPrimaryContainer,
    secondary = RedAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFFFB4AB), // Lighter error for red theme
    onError = Color(0xFF1F1F1F)
)

// Blue Theme
private val BlueDarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = BlueAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Pink Theme
private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkPrimary,
    onPrimary = PinkOnPrimary,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = PinkOnPrimaryContainer,
    secondary = PinkAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// ============================================
// MUTED COLOR SCHEMES (Less saturated, softer look)
// ============================================

// Muted Cyan Theme
private val MutedCyanDarkColorScheme = darkColorScheme(
    primary = MutedCyanPrimary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF2D4A47),
    onPrimaryContainer = Color(0xFFB8E0D8),
    secondary = MutedCyanAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Purple Theme
private val MutedPurpleDarkColorScheme = darkColorScheme(
    primary = MutedPurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = Color(0xFF3D3352),
    onPrimaryContainer = Color(0xFFD8CCE8),
    secondary = MutedPurpleAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Orange Theme
private val MutedOrangeDarkColorScheme = darkColorScheme(
    primary = MutedOrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = Color(0xFF4A3D2D),
    onPrimaryContainer = Color(0xFFE8D8C8),
    secondary = MutedOrangeAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Green Theme
private val MutedGreenDarkColorScheme = darkColorScheme(
    primary = MutedGreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = Color(0xFF2D4A38),
    onPrimaryContainer = Color(0xFFC8E8D4),
    secondary = MutedGreenAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Red Theme
private val MutedRedDarkColorScheme = darkColorScheme(
    primary = MutedRedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = Color(0xFF4A2D2D),
    onPrimaryContainer = Color(0xFFE8D0D0),
    secondary = MutedRedAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Blue Theme
private val MutedBlueDarkColorScheme = darkColorScheme(
    primary = MutedBluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = Color(0xFF2D3D4A),
    onPrimaryContainer = Color(0xFFD0E0F0),
    secondary = MutedBlueAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

// Muted Pink Theme
private val MutedPinkDarkColorScheme = darkColorScheme(
    primary = MutedPinkPrimary,
    onPrimary = PinkOnPrimary,
    primaryContainer = Color(0xFF4A2D3D),
    onPrimaryContainer = Color(0xFFE8D0DC),
    secondary = MutedPinkAccent,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color(0xFF1F1F1F)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error
)

/**
 * Get the color scheme for a specific theme
 */
fun getColorSchemeForTheme(theme: AppTheme, vibrant: Boolean = true): androidx.compose.material3.ColorScheme {
    return if (vibrant) {
        when (theme) {
            AppTheme.CYAN -> CyanDarkColorScheme
            AppTheme.PURPLE -> PurpleDarkColorScheme
            AppTheme.ORANGE -> OrangeDarkColorScheme
            AppTheme.GREEN -> GreenDarkColorScheme
            AppTheme.RED -> RedDarkColorScheme
            AppTheme.BLUE -> BlueDarkColorScheme
            AppTheme.PINK -> PinkDarkColorScheme
        }
    } else {
        when (theme) {
            AppTheme.CYAN -> MutedCyanDarkColorScheme
            AppTheme.PURPLE -> MutedPurpleDarkColorScheme
            AppTheme.ORANGE -> MutedOrangeDarkColorScheme
            AppTheme.GREEN -> MutedGreenDarkColorScheme
            AppTheme.RED -> MutedRedDarkColorScheme
            AppTheme.BLUE -> MutedBlueDarkColorScheme
            AppTheme.PINK -> MutedPinkDarkColorScheme
        }
    }
}

/**
 * Get the accent color for a specific theme (used for previews)
 */
fun getAccentColorForTheme(theme: AppTheme, vibrant: Boolean = true): Color {
    return if (vibrant) {
        when (theme) {
            AppTheme.CYAN -> NeonCyan
            AppTheme.PURPLE -> PurplePrimary
            AppTheme.ORANGE -> OrangePrimary
            AppTheme.GREEN -> GreenPrimary
            AppTheme.RED -> RedPrimary
            AppTheme.BLUE -> BluePrimary
            AppTheme.PINK -> PinkPrimary
        }
    } else {
        when (theme) {
            AppTheme.CYAN -> MutedCyanPrimary
            AppTheme.PURPLE -> MutedPurplePrimary
            AppTheme.ORANGE -> MutedOrangePrimary
            AppTheme.GREEN -> MutedGreenPrimary
            AppTheme.RED -> MutedRedPrimary
            AppTheme.BLUE -> MutedBluePrimary
            AppTheme.PINK -> MutedPinkPrimary
        }
    }
}

@Composable
fun WorkoutTrackerTheme(
    appTheme: AppTheme = AppTheme.CYAN,
    vibrantColors: Boolean = true,
    darkTheme: Boolean = true, // Force dark theme by default
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to use our custom palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getColorSchemeForTheme(appTheme, vibrantColors)
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
