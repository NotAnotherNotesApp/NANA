package com.allubie.nana.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    secondary = Primary,
    onSecondary = OnPrimary,
    secondaryContainer = Primary.copy(alpha = 0.12f),
    onSecondaryContainer = PrimaryDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = Error,
    onError = OnError,
    outline = OnSurfaceVariantLight.copy(alpha = 0.5f),
    outlineVariant = OnSurfaceVariantLight.copy(alpha = 0.2f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = Primary,
    secondary = Primary,
    onSecondary = OnPrimary,
    secondaryContainer = Primary.copy(alpha = 0.12f),
    onSecondaryContainer = Primary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = Error,
    onError = OnError,
    outline = OnSurfaceVariantDark.copy(alpha = 0.5f),
    outlineVariant = OnSurfaceVariantDark.copy(alpha = 0.2f)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = Primary,
    secondary = Primary,
    onSecondary = OnPrimary,
    secondaryContainer = Primary.copy(alpha = 0.12f),
    onSecondaryContainer = Primary,
    background = BackgroundAmoled,
    onBackground = OnBackgroundDark,
    surface = SurfaceAmoled,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantAmoled,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = Error,
    onError = OnError,
    outline = OnSurfaceVariantDark.copy(alpha = 0.5f),
    outlineVariant = OnSurfaceVariantDark.copy(alpha = 0.2f)
)

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}

@Composable
fun NanaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> {
            if (supportsDynamicColor) dynamicLightColorScheme(context)
            else LightColorScheme
        }
        ThemeMode.DARK -> {
            if (supportsDynamicColor) dynamicDarkColorScheme(context)
            else DarkColorScheme
        }
        ThemeMode.AMOLED -> AmoledColorScheme // Fixed colors for AMOLED
        ThemeMode.SYSTEM -> {
            if (systemDarkTheme) {
                if (supportsDynamicColor) dynamicDarkColorScheme(context)
                else DarkColorScheme
            } else {
                if (supportsDynamicColor) dynamicLightColorScheme(context)
                else LightColorScheme
            }
        }
    }
    
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        ThemeMode.SYSTEM -> systemDarkTheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
