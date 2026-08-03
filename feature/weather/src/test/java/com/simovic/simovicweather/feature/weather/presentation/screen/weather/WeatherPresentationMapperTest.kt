package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.weather.R
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.DailyWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class WeatherPresentationMapperTest {
    private val mapper =
        WeatherPresentationMapper(
            WeatherDateFormatter(
                LocalizedDatePatternProvider { _, _ -> "EEE, MMM d" },
                localeProvider = { Locale.US },
            ),
        )

    @Test
    fun `maps location and rounded current weather measurements`() {
        val forecast =
            forecast().copy(
                location =
                    WeatherLocation(
                        name = "Sarajevo",
                        region = "Sarajevo",
                        country = "Bosnia and Herzegovina",
                        coordinates = Coordinates(43.85, 18.41),
                    ),
            )

        val result = mapper.toUiModel(forecast)

        assertEquals("Sarajevo, Bosnia and Herzegovina", result.locationName)
        assertEquals(23, result.current.temperatureCelsius)
        assertEquals(24, result.current.apparentTemperatureCelsius)
        assertEquals(55, result.current.humidityPercent)
        assertEquals(0.5, result.current.precipitationMillimeters, 0.0)
        assertEquals(1015, result.current.pressureHectopascals)
        assertEquals(11, result.current.windSpeedKilometersPerHour)
        assertFalse(result.updatedAt.contains("2026-07-31"))
    }

    @Test
    fun `leaves missing location name blank for UI fallback`() {
        val location = WeatherLocation(name = "", coordinates = Coordinates(43.85, 18.41))

        val result = mapper.toUiModel(forecast().copy(location = location))

        assertEquals("", result.locationName)
    }

    @Test
    fun `maps today temperatures and seven separate daily ranges`() {
        val result = mapper.toUiModel(forecast())

        assertEquals(15, result.todayMinimumTemperatureCelsius)
        assertEquals(25, result.todayMaximumTemperatureCelsius)
        assertEquals(7, result.days.size)
        assertTrue(result.days.first().isToday)
        assertTrue(result.days.drop(1).none(DailyWeatherUiModel::isToday))
        assertEquals(15, result.days.first().minimumTemperatureCelsius)
        assertEquals(25, result.days.first().maximumTemperatureCelsius)
    }

    @Test
    fun `empty daily forecast has no today temperatures`() {
        val result = mapper.toUiModel(forecast().copy(days = emptyList()))

        assertNull(result.todayMinimumTemperatureCelsius)
        assertNull(result.todayMaximumTemperatureCelsius)
        assertTrue(result.days.isEmpty())
    }

    @Test
    fun `maps representative WMO conditions`() {
        val expected =
            mapOf(
                0 to (R.string.weather_condition_clear to WeatherIcon.CLEAR),
                2 to (R.string.weather_condition_partly_cloudy to WeatherIcon.PARTLY_CLOUDY),
                3 to (R.string.weather_condition_overcast to WeatherIcon.CLOUDY),
                45 to (R.string.weather_condition_fog to WeatherIcon.FOG),
                61 to (R.string.weather_condition_rain to WeatherIcon.RAIN),
                66 to (R.string.weather_condition_freezing_rain to WeatherIcon.FREEZING_RAIN),
                71 to (R.string.weather_condition_snow to WeatherIcon.SNOW),
                95 to (R.string.weather_condition_thunderstorm to WeatherIcon.THUNDERSTORM),
            )

        expected.forEach { (code, descriptionAndIcon) ->
            val condition = mapper.toUiModel(forecast(weatherCode = code)).current.condition
            assertEquals(descriptionAndIcon.first, condition.descriptionRes)
            assertEquals(descriptionAndIcon.second, condition.icon)
        }
    }

    @Test
    fun `unknown WMO condition maps to fallback`() {
        val condition = mapper.toUiModel(forecast(weatherCode = 500)).current.condition

        assertEquals(R.string.weather_condition_unknown, condition.descriptionRes)
        assertEquals(WeatherIcon.UNKNOWN, condition.icon)
    }

    private fun forecast(weatherCode: Int = 1): WeatherForecast =
        WeatherForecast(
            location = WeatherLocation(name = "Sarajevo", coordinates = Coordinates(43.85, 18.41)),
            current =
                CurrentWeather(
                    time = LocalDateTime.of(2026, 7, 31, 12, 0),
                    temperature = 22.5,
                    apparentTemperature = 23.5,
                    humidity = 55,
                    precipitation = 0.5,
                    weatherCode = weatherCode,
                    pressure = 1015.2,
                    windSpeed = 10.6,
                ),
            days = List(7, ::dailyWeather),
        )

    private fun dailyWeather(index: Int) =
        DailyWeather(
            date = LocalDate.of(2026, 8, index + 1),
            weatherCode = index,
            minTemperature = 15.0 + index,
            maxTemperature = 25.0 + index,
            precipitationProbability = 10,
        )
}
