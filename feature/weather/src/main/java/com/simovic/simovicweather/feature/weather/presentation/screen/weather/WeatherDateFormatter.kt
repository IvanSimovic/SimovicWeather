package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal class WeatherDateFormatter(
    private val patternProvider: LocalizedDatePatternProvider,
    private val localeProvider: () -> Locale,
) {
    constructor() : this(
        patternProvider =
            LocalizedDatePatternProvider { locale, skeleton ->
                DateFormat.getBestDateTimePattern(locale, skeleton)
            },
        localeProvider = { Locale.getDefault(Locale.Category.FORMAT) },
    )

    fun formatDate(value: LocalDate): String {
        val locale = localeProvider()
        val pattern = patternProvider.getPattern(locale, DISPLAY_DATE_SKELETON)
        return value.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun formatTime(value: LocalDateTime): String =
        value.format(
            DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(localeProvider()),
        )

    private companion object {
        const val DISPLAY_DATE_SKELETON = "EEEMMMd"
    }
}

internal fun interface LocalizedDatePatternProvider {
    fun getPattern(
        locale: Locale,
        skeleton: String,
    ): String
}
