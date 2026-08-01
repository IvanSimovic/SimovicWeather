package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation

@Immutable
internal data class WeatherUiModel(
    val locationName: String,
    val updatedAt: String,
    val current: CurrentWeatherUiModel,
    val todayMinimumTemperatureCelsius: Int?,
    val todayMaximumTemperatureCelsius: Int?,
    val days: List<DailyWeatherUiModel>,
)

@Immutable
internal data class CurrentWeatherUiModel(
    val temperatureCelsius: Int,
    val apparentTemperatureCelsius: Int,
    val humidityPercent: Int,
    val condition: WeatherConditionUiModel,
    val scene: WeatherScene,
    val precipitationMillimeters: Double,
    val pressureHectopascals: Int,
    val windSpeedKilometersPerHour: Int,
)

@Immutable
internal data class DailyWeatherUiModel(
    val date: String,
    val isToday: Boolean,
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
    @get:StringRes val descriptionRes: Int,
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

internal enum class WeatherScene {
    SUNNY,
    SUNNY_WINDY,
    RAIN,
    RAIN_WINDY,
}
