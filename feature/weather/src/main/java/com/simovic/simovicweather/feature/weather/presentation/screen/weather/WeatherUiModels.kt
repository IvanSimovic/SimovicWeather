package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Immutable
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation

@Immutable
internal data class WeatherUiModel(
    val locationName: String,
    val current: CurrentWeatherUiModel,
    val days: List<DailyWeatherUiModel>,
)

@Immutable
internal data class CurrentWeatherUiModel(
    val time: String,
    val temperatureCelsius: Int,
    val apparentTemperatureCelsius: Int,
    val humidityPercent: Int,
    val condition: WeatherConditionUiModel,
    val precipitationMillimeters: Double,
    val pressureHectopascals: Int,
    val windSpeedKilometersPerHour: Int,
)

@Immutable
internal data class DailyWeatherUiModel(
    val date: String,
    val minimumTemperatureCelsius: Int,
    val maximumTemperatureCelsius: Int,
    val precipitationProbabilityPercent: Int,
    val condition: WeatherConditionUiModel,
)

@Immutable
internal data class LocationUiModel(
    val label: String,
    internal val location: WeatherLocation,
)

@Immutable
internal data class WeatherConditionUiModel(
    val description: String,
    val icon: WeatherIcon,
)

internal enum class WeatherIcon {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    THUNDERSTORM,
    UNKNOWN,
}
