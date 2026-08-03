package com.simovic.simovicweather.feature.weather.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ForecastResponse(
    val current: CurrentWeatherApiModel,
    val daily: DailyWeatherApiModel,
)

@Serializable
internal data class CurrentWeatherApiModel(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    val precipitation: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("pressure_msl") val pressure: Double,
    @SerialName("wind_speed_10m") val windSpeed: Double,
)

@Serializable
internal data class DailyWeatherApiModel(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val maxTemperature: List<Double>,
    @SerialName("temperature_2m_min") val minTemperature: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbability: List<Int>,
)
