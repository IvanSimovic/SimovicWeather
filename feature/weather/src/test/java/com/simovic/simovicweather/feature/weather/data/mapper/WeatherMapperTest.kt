package com.simovic.simovicweather.feature.weather.data.mapper

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.api.model.CurrentWeatherApiModel
import com.simovic.simovicweather.feature.weather.data.datasource.api.model.DailyWeatherApiModel
import com.simovic.simovicweather.feature.weather.data.datasource.api.model.ForecastResponse
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.DailyWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class WeatherMapperTest {
    private val mapper = WeatherMapper()
    private val location = WeatherLocation(name = "Sarajevo", coordinates = Coordinates(43.85, 18.41))

    @Test
    fun `maps forecast response`() {
        val result = mapper.toDomain(forecastResponse(), location)

        assertEquals(
            Result.Success(
                WeatherForecast(
                    location = location,
                    current =
                        CurrentWeather(
                            time = LocalDateTime.of(2026, 7, 31, 12, 0),
                            temperature = 22.5,
                            apparentTemperature = 23.0,
                            humidity = 55,
                            precipitation = 0.0,
                            weatherCode = 1,
                            pressure = 1015.0,
                            windSpeed = 10.0,
                        ),
                    days =
                        List(7) { index ->
                            DailyWeather(
                                date = LocalDate.of(2026, 8, index + 1),
                                weatherCode = index,
                                minTemperature = 15.0 + index,
                                maxTemperature = 25.0 + index,
                                precipitationProbability = 10,
                            )
                        },
                ),
            ),
            result,
        )
    }

    @Test
    fun `rejects mismatched daily arrays`() {
        val response = forecastResponse().let { it.copy(daily = it.daily.copy(weatherCode = listOf(0))) }

        val result = mapper.toDomain(response, location)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).reason is AppFailure.MalformedData)
    }

    @Test
    fun `rejects malformed current time`() {
        val response = forecastResponse().let { it.copy(current = it.current.copy(time = "2026-07-31T12:00junk")) }

        val result = mapper.toDomain(response, location)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).reason is AppFailure.MalformedData)
    }

    @Test
    fun `rejects invalid daily date`() {
        val response = forecastResponse().let { it.copy(daily = it.daily.copy(time = listOf("2026-02-30"))) }

        val result = mapper.toDomain(response, location)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).reason is AppFailure.MalformedData)
    }

    private fun forecastResponse(): ForecastResponse =
        ForecastResponse(
            current =
                CurrentWeatherApiModel(
                    time = "2026-07-31T12:00",
                    temperature = 22.5,
                    humidity = 55,
                    apparentTemperature = 23.0,
                    precipitation = 0.0,
                    weatherCode = 1,
                    pressure = 1015.0,
                    windSpeed = 10.0,
                ),
            daily =
                DailyWeatherApiModel(
                    time = List(7) { "2026-08-0${it + 1}" },
                    weatherCode = List(7) { it },
                    maxTemperature = List(7) { 25.0 + it },
                    minTemperature = List(7) { 15.0 + it },
                    precipitationProbability = List(7) { 10 },
                ),
        )
}
