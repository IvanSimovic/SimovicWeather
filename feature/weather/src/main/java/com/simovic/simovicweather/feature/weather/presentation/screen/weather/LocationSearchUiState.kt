package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Immutable

@Immutable
internal data class LocationSearchUiState(
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
