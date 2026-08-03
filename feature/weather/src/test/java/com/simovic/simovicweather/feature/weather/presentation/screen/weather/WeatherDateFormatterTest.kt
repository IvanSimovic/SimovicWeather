package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class WeatherDateFormatterTest {
    @Test
    fun `formats requested fields using localized pattern ordering`() {
        val formatter =
            WeatherDateFormatter(
                LocalizedDatePatternProvider { _, _ -> "d MMM EEE" },
                localeProvider = { Locale.US },
            )

        val result = formatter.formatDate(LocalDate.of(2026, 8, 1))

        assertEquals("1 Aug Sat", result)
    }

    @Test
    fun `requests weekday abbreviated month and day without year`() {
        var requestedLocale: Locale? = null
        var requestedSkeleton = ""
        val formatter =
            WeatherDateFormatter(
                LocalizedDatePatternProvider { locale, skeleton ->
                    requestedLocale = locale
                    requestedSkeleton = skeleton
                    "'localized date'"
                },
                localeProvider = { Locale.GERMANY },
            )

        val result = formatter.formatDate(LocalDate.of(2026, 8, 1))

        assertEquals(Locale.GERMANY, requestedLocale)
        assertEquals("EEEMMMd", requestedSkeleton)
        assertEquals("localized date", result)
    }

    @Test
    fun `formats current observation using localized short time`() {
        val formatter =
            WeatherDateFormatter(
                patternProvider = LocalizedDatePatternProvider { _, _ -> error("Pattern should not be requested") },
                localeProvider = { Locale.US },
            )

        val result = formatter.formatTime(LocalDateTime.of(2026, 8, 1, 12, 30))

        assertTrue(result.contains("12:30"))
    }
}
