package com.bizmanager.presentation.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppPrimary = Color(0xFF0F766E)
private val AppSecondary = Color(0xFFB45309)
private val AppSurface = Color(0xFFF8F5EF)
private val AppSurfaceVariant = Color(0xFFE7E1D6)
private val AppBackground = Color(0xFFF4EFE6)
private val AppCard = Color(0xFFFFFBF5)
private val AppText = Color(0xFF1F2937)
private val AppMuted = Color(0xFF6B7280)
private val AppError = Color(0xFFB91C1C)

private val Material3Colors: ColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    secondary = AppSecondary,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = AppText,
    surface = AppCard,
    onSurface = AppText,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppMuted,
    outline = Color(0xFFD0C7BA),
    error = AppError,
    onError = Color.White
)

private val Material2Colors: Colors = lightColors(
    primary = AppPrimary,
    primaryVariant = Color(0xFF115E59),
    secondary = AppSecondary,
    secondaryVariant = Color(0xFF92400E),
    background = AppBackground,
    surface = AppSurface,
    error = AppError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AppText,
    onSurface = AppText,
    onError = Color.White
)

@Composable
fun BizManagerTheme(content: @Composable () -> Unit) {
    Material3Theme(
        colorScheme = Material3Colors,
        typography = Typography()
    ) {
        Material2Theme(colors = Material2Colors) {
            Surface(color = Material3Theme.colorScheme.background) {
                content()
            }
        }
    }
}
