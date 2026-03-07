package com.allubie.nana.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders

val NanaWidgetColorProviders = ColorProviders(
    light = lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF6750A4),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEADDFF),
        onSecondaryContainer = Color(0xFF21005D),
        background = Color(0xFFFDFBFF),
        onBackground = Color(0xFF1D1C20),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1D1C20),
        surfaceVariant = Color(0xFFE9E7EC),
        onSurfaceVariant = Color(0xFF5E5D62),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        outline = Color(0xFFE1DFE4),
        inverseSurface = Color(0xFF1D1C20),
        inverseOnSurface = Color(0xFFFDFBFF),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFD0BCFF),
        onSecondary = Color(0xFF381E72),
        secondaryContainer = Color(0xFF4F378B),
        onSecondaryContainer = Color(0xFFEADDFF),
        background = Color(0xFF1A1C1E),
        onBackground = Color(0xFFE4E2E6),
        surface = Color(0xFF2A2F33),
        onSurface = Color(0xFFE4E2E6),
        surfaceVariant = Color(0xFF3A3F44),
        onSurfaceVariant = Color(0xFFA3A0A5),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        outline = Color(0xFF4A4A4D),
        inverseSurface = Color(0xFFFDFBFF),
        inverseOnSurface = Color(0xFF1A1C1E),
    )
)
