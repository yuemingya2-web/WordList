package com.vocabmaster.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Gray99,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Teal40,
    onSecondary = Gray99,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Orange40,
    onTertiary = Gray99,
    tertiaryContainer = Orange90,
    onTertiaryContainer = Orange10,
    background = Gray99,
    onBackground = Gray10,
    surface = Gray99,
    onSurface = Gray10,
    surfaceVariant = Gray90,
    onSurfaceVariant = Gray20,
    error = Orange40,
    onError = Gray99,
    errorContainer = Orange90,
    onErrorContainer = Orange10
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = Orange80,
    onTertiary = Orange20,
    tertiaryContainer = Orange40,
    onTertiaryContainer = Orange90,
    background = Gray10,
    onBackground = Gray90,
    surface = Gray10,
    onSurface = Gray90,
    surfaceVariant = Gray20,
    onSurfaceVariant = Gray90,
    error = Orange80,
    onError = Orange20,
    errorContainer = Orange40,
    onErrorContainer = Orange90
)

@Composable
fun VocabMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
