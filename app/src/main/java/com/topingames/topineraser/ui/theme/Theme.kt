package com.topingames.topineraser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val TopinDarkColorScheme = darkColorScheme(
    primary            = TopinTeal,
    onPrimary          = TopinDark,
    primaryContainer   = TopinDarkSurf,
    onPrimaryContainer = TopinTealLight,
    secondary          = TopinTealDark,
    onSecondary        = TopinOnDark,
    background         = TopinDark,
    onBackground       = TopinOnDark,
    surface            = TopinDarkSurf,
    onSurface          = TopinOnDark,
    surfaceVariant     = Color(0xFF374151),
    onSurfaceVariant   = Color(0xFF9CA3AF),
    tertiary           = TopinSuccess,
    onTertiary         = TopinDark,
)

private val TopinLightColorScheme = lightColorScheme(
    primary            = TopinTealDark,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFCCF5F1),
    onPrimaryContainer = TopinTealDark,
    secondary          = TopinTeal,
    onSecondary        = Color.White,
    background         = Color(0xFFF9FAFB),
    onBackground       = Color(0xFF111827),
    surface            = Color.White,
    onSurface          = Color(0xFF111827),
    surfaceVariant     = Color(0xFFE5E7EB),
    onSurfaceVariant   = Color(0xFF6B7280),
    tertiary           = TopinSuccessDk,
    onTertiary         = Color.White,
)

@Composable
fun TopinEraserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // disabled — brand colors always
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> TopinDarkColorScheme
        else      -> TopinLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
