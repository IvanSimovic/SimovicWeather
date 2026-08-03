package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import kotlin.math.roundToInt

internal class WeatherPresentationMapper(
    private val dateFormatter: WeatherDateFormatter,
) {
    fun toUiModel(forecast: WeatherForecast): WeatherUiModel {
        val current = forecast.current
        return WeatherUiModel(
            locationName = locationLabel(forecast.location),
            current =
                CurrentWeatherUiModel(
                    time = dateFormatter.formatTime(current.time),
                    temperatureCelsius = current.temperature.roundToInt(),
                    apparentTemperatureCelsius = current.apparentTemperature.roundToInt(),
                    humidityPercent = current.humidity,
                    condition = condition(current.weatherCode),
                    precipitationMillimeters = current.precipitation,
                    pressureHectopascals = current.pressure.roundToInt(),
                    windSpeedKilometersPerHour = current.windSpeed.roundToInt(),
                ),
            days =
                forecast.days.map { day ->
                    DailyWeatherUiModel(
                        date = dateFormatter.formatDate(day.date),
                        minimumTemperatureCelsius = day.minTemperature.roundToInt(),
                        maximumTemperatureCelsius = day.maxTemperature.roundToInt(),
                        precipitationProbabilityPercent = day.precipitationProbability,
                        condition = condition(day.weatherCode),
                    )
                },
        )
    }

    fun toLocationUiModel(location: WeatherLocation) = LocationUiModel(locationLabel(location), location)

    fun toError(failure: AppFailure): WeatherErrorReason =
        when (failure) {
            is AppFailure.Connectivity -> WeatherErrorReason.NETWORK
            is AppFailure.RateLimited -> WeatherErrorReason.RATE_LIMITED
            is AppFailure.MalformedData -> WeatherErrorReason.MALFORMED_DATA
            is AppFailure.Server -> WeatherErrorReason.SERVER
            AppFailure.LocationUnavailable -> WeatherErrorReason.LOCATION_UNAVAILABLE
            is AppFailure.Unknown,
            AppFailure.PermissionDenied,
            -> WeatherErrorReason.UNKNOWN
        }

    private fun locationLabel(location: WeatherLocation): String =
        listOf(location.name.ifBlank { "Current location" }, location.region, location.country)
            .filterNotNull()
            .filter(String::isNotBlank)
            .distinct()
            .joinToString()

    private fun condition(code: Int): WeatherConditionUiModel =
        when (code) {
            CODE_CLEAR -> WeatherConditionUiModel("Clear sky", WeatherIcon.CLEAR)
            in CODE_PARTLY_CLOUDY_START..CODE_PARTLY_CLOUDY_END ->
                WeatherConditionUiModel("Partly cloudy", WeatherIcon.PARTLY_CLOUDY)
            CODE_OVERCAST -> WeatherConditionUiModel("Overcast", WeatherIcon.CLOUDY)
            CODE_FOG, CODE_RIME_FOG -> WeatherConditionUiModel("Fog", WeatherIcon.FOG)
            in CODE_FREEZING_DRIZZLE_START..CODE_FREEZING_DRIZZLE_END,
            in CODE_FREEZING_RAIN_START..CODE_FREEZING_RAIN_END,
            -> WeatherConditionUiModel("Freezing rain", WeatherIcon.FREEZING_RAIN)
            in CODE_DRIZZLE_START..CODE_DRIZZLE_END,
            in CODE_RAIN_START..CODE_RAIN_END,
            in CODE_SHOWER_START..CODE_SHOWER_END,
            -> WeatherConditionUiModel("Rain", WeatherIcon.RAIN)
            in CODE_SNOW_START..CODE_SNOW_END,
            in CODE_SNOW_SHOWER_START..CODE_SNOW_SHOWER_END,
            -> WeatherConditionUiModel("Snow", WeatherIcon.SNOW)
            in CODE_THUNDERSTORM_START..CODE_THUNDERSTORM_END ->
                WeatherConditionUiModel("Thunderstorm", WeatherIcon.THUNDERSTORM)
            else -> WeatherConditionUiModel("Unknown", WeatherIcon.UNKNOWN)
        }

    private companion object {
        const val CODE_CLEAR = 0
        const val CODE_PARTLY_CLOUDY_START = 1
        const val CODE_PARTLY_CLOUDY_END = 2
        const val CODE_OVERCAST = 3
        const val CODE_FOG = 45
        const val CODE_RIME_FOG = 48
        const val CODE_DRIZZLE_START = 51
        const val CODE_DRIZZLE_END = 55
        const val CODE_FREEZING_DRIZZLE_START = 56
        const val CODE_FREEZING_DRIZZLE_END = 57
        const val CODE_RAIN_START = 61
        const val CODE_RAIN_END = 65
        const val CODE_FREEZING_RAIN_START = 66
        const val CODE_FREEZING_RAIN_END = 67
        const val CODE_SNOW_START = 71
        const val CODE_SNOW_END = 77
        const val CODE_SHOWER_START = 80
        const val CODE_SHOWER_END = 82
        const val CODE_SNOW_SHOWER_START = 85
        const val CODE_SNOW_SHOWER_END = 86
        const val CODE_THUNDERSTORM_START = 95
        const val CODE_THUNDERSTORM_END = 99
    }
}
