@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.simovic.simovicweather.feature.base.presentation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalWeatherColors =
    staticCompositionLocalOf<WeatherColors> {
        error("No WeatherColors provided")
    }

private val LocalWeatherDimensions =
    staticCompositionLocalOf<WeatherDimensions> {
        error("No WeatherDimensions provided")
    }

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkWeatherColors else LightWeatherColors
    val colorScheme =
        if (darkTheme) {
            darkColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                background = colors.backgroundStart,
                onBackground = colors.textPrimary,
                surface = colors.card,
                onSurface = colors.textPrimary,
                surfaceVariant = colors.cardSecondary,
                onSurfaceVariant = colors.textSecondary,
                error = colors.error,
            )
        } else {
            lightColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                background = colors.backgroundStart,
                onBackground = colors.textPrimary,
                surface = colors.card,
                onSurface = colors.textPrimary,
                surfaceVariant = colors.cardSecondary,
                onSurfaceVariant = colors.textSecondary,
                error = colors.error,
            )
        }
    CompositionLocalProvider(
        LocalWeatherColors provides colors,
        LocalWeatherDimensions provides AppWeatherDimensions,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

object AppTheme {
    val colors: WeatherColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWeatherColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val dimensions: WeatherDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalWeatherDimensions.current
}
