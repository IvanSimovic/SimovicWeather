package com.simovic.simovicweather.feature.weather.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

data class WeatherLocation(
    val id: Long? = null,
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val coordinates: Coordinates,
)

data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val pressure: Double,
    val windSpeed: Double,
)

data class DailyWeather(
    val date: LocalDate,
    val weatherCode: Int,
    val minTemperature: Double,
    val maxTemperature: Double,
    val precipitationProbability: Int,
)

data class WeatherForecast(
    val location: WeatherLocation,
    val current: CurrentWeather,
    val days: List<DailyWeather>,
)
