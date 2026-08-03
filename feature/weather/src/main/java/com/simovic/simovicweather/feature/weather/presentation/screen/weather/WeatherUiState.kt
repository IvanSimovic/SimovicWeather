package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Immutable

@Immutable
internal data class WeatherScreenUiState(
    val target: ForecastTarget = ForecastTarget.CurrentLocation,
    val forecast: WeatherUiState = WeatherUiState.Initial,
    val search: LocationSearchUiState = LocationSearchUiState(),
    val refresh: RefreshUiState = RefreshUiState.Idle,
)

@Immutable
internal sealed interface ForecastTarget {
    data object CurrentLocation : ForecastTarget

    data class SelectedLocation(
        val location: LocationUiModel,
    ) : ForecastTarget
}

@Immutable
internal sealed interface RefreshUiState {
    data object Idle : RefreshUiState

    data object Refreshing : RefreshUiState

    data object UpToDate : RefreshUiState

    data class Failed(
        val reason: WeatherErrorReason,
    ) : RefreshUiState
}

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
