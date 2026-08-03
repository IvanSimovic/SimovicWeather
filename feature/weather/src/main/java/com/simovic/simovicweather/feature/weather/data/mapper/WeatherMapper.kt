package com.simovic.simovicweather.feature.weather.data.mapper

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.api.model.ForecastResponse
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.DailyWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

internal class WeatherMapper {
    fun toDomain(
        response: ForecastResponse,
        location: WeatherLocation,
    ): Result<WeatherForecast> {
        if (!hasValidDailyArrays(response)) return Result.Failure(AppFailure.MalformedData())
        val forecast = mapForecast(response, location) ?: return Result.Failure(AppFailure.MalformedData())

        return Result.Success(forecast)
    }

    private fun mapForecast(
        response: ForecastResponse,
        location: WeatherLocation,
    ): WeatherForecast? {
        val current = mapCurrent(response) ?: return null
        val days = mapDays(response) ?: return null

        return WeatherForecast(
            location = location,
            current = current,
            days = days,
        )
    }

    private fun hasValidDailyArrays(response: ForecastResponse): Boolean {
        val daily = response.daily
        val expectedSize = daily.time.size
        val sizes =
            listOf(
                daily.weatherCode.size,
                daily.maxTemperature.size,
                daily.minTemperature.size,
                daily.precipitationProbability.size,
            )
        return expectedSize > 0 && sizes.all { it == expectedSize }
    }

    private fun mapCurrent(response: ForecastResponse): CurrentWeather? =
        response.current.let { current ->
            val time = current.time.toLocalDateTimeOrNull() ?: return null
            CurrentWeather(
                time = time,
                temperature = current.temperature,
                apparentTemperature = current.apparentTemperature,
                humidity = current.humidity,
                precipitation = current.precipitation,
                weatherCode = current.weatherCode,
                pressure = current.pressure,
                windSpeed = current.windSpeed,
            )
        }

    private fun mapDays(response: ForecastResponse): List<DailyWeather>? =
        response.daily.let { daily ->
            val dates = daily.time.map { it.toLocalDateOrNull() ?: return null }
            List(daily.time.size) { index ->
                DailyWeather(
                    date = dates[index],
                    weatherCode = daily.weatherCode[index],
                    minTemperature = daily.minTemperature[index],
                    maxTemperature = daily.maxTemperature[index],
                    precipitationProbability = daily.precipitationProbability[index],
                )
            }
        }

    private fun String.toLocalDateOrNull(): LocalDate? =
        try {
            LocalDate.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
        try {
            LocalDateTime.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }
}
