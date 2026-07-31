package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Immutable

@Immutable
internal data class WeatherScreenUiState(
    val forecast: WeatherUiState = WeatherUiState.Initial,
    val search: LocationSearchUiState = LocationSearchUiState(),
)

@Immutable
internal sealed interface WeatherUiState {
    data object Initial : WeatherUiState

    data object Loading : WeatherUiState

    data class Content(
        val weather: WeatherUiModel,
    ) : WeatherUiState

    data class Error(
        val reason: WeatherErrorReason,
        val canRetry: Boolean,
    ) : WeatherUiState
}

@Immutable
internal data class LocationSearchUiState(
    val isVisible: Boolean = false,
    val query: String = "",
    val status: LocationSearchStatus = LocationSearchStatus.Idle,
    val isLocationPermissionDenied: Boolean = false,
)

@Immutable
internal sealed interface LocationSearchStatus {
    data object Idle : LocationSearchStatus

    data object QueryTooShort : LocationSearchStatus

    data object Searching : LocationSearchStatus

    data class Results(
        val locations: List<LocationUiModel>,
    ) : LocationSearchStatus

    data object NoResults : LocationSearchStatus

    data class Failed(
        val reason: WeatherErrorReason,
    ) : LocationSearchStatus
}

internal enum class WeatherErrorReason {
    NETWORK,
    RATE_LIMITED,
    LOCATION_UNAVAILABLE,
    MALFORMED_DATA,
    SERVER,
    UNKNOWN,
}
