@file:Suppress("MagicNumber")

package com.simovic.simovicweather.feature.base.presentation.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class WeatherColors(
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val card: Color,
    val cardSecondary: Color,
    val cardBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val sun: Color,
    val rain: Color,
    val error: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
)

internal val DarkWeatherColors =
    WeatherColors(
        backgroundStart = Color(0xFF071524),
        backgroundEnd = Color(0xFF134A73),
        card = Color(0xCC0A111C),
        cardSecondary = Color(0xB31A2C3E),
        cardBorder = Color(0x33FFFFFF),
        primary = Color(0xFF66C7FF),
        onPrimary = Color(0xFF001E2F),
        textPrimary = Color(0xFFF5F9FF),
        textSecondary = Color(0xFFB9C8D8),
        sun = Color(0xFFFFC83D),
        rain = Color(0xFF69B9FF),
        error = Color(0xFFFFB4AB),
        disabledContainer = Color(0xFF405564),
        disabledContent = Color(0xFF9AAAB6),
    )

internal val LightWeatherColors =
    WeatherColors(
        backgroundStart = Color(0xFFDDF3FF),
        backgroundEnd = Color(0xFF8DCEF4),
        card = Color(0xD9FFFFFF),
        cardSecondary = Color(0xBFFFFFFF),
        cardBorder = Color(0x330B2230),
        primary = Color(0xFF006493),
        onPrimary = Color.White,
        textPrimary = Color(0xFF102A3B),
        textSecondary = Color(0xFF49616F),
        sun = Color(0xFFE9A900),
        rain = Color(0xFF1677A8),
        error = Color(0xFFBA1A1A),
        disabledContainer = Color(0xFFCAD8E0),
        disabledContent = Color(0xFF6F7F89),
    )
