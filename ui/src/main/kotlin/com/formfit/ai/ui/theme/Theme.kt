package com.formfit.ai.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = FormFitGreen,
    onPrimary = FormFitNavy,
    primaryContainer = FormFitGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = FormFitBlue,
    onSecondary = FormFitNavy,
    secondaryContainer = FormFitBlueDark,
    onSecondaryContainer = TextPrimary,
    tertiary = FormFitPurple,
    background = FormFitNavy,
    onBackground = TextPrimary,
    surface = FormFitSurface,
    onSurface = TextPrimary,
    surfaceVariant = FormFitSurface2,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = ErrorRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = FormFitGreenDark,
    onPrimary = FormFitNavy,
    primaryContainer = FormFitGreenLight,
    onPrimaryContainer = FormFitNavy,
    secondary = FormFitBlueDark,
    onSecondary = FormFitNavy,
    background = FormFitNavy,
    onBackground = TextPrimary,
    surface = FormFitSurface,
    onSurface = TextPrimary,
)

@Composable
fun FormFitTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FormFitNavy.toArgb()
            window.navigationBarColor = NavBarBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FormFitTypography,
        content = content
    )
}
