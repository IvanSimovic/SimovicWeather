package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface WeatherUiState {
    val search: SearchUiState

    data class Initial(
        override val search: SearchUiState = SearchUiState.Idle,
    ) : WeatherUiState

    data class Loading(
        override val search: SearchUiState = SearchUiState.Idle,
    ) : WeatherUiState

    data class Content(
        val weather: WeatherUiModel,
        override val search: SearchUiState = SearchUiState.Idle,
    ) : WeatherUiState

    data class PermissionRequired(
        override val search: SearchUiState = SearchUiState.Idle,
    ) : WeatherUiState

    data class Error(
        val reason: WeatherErrorReason,
        val canRetry: Boolean,
        override val search: SearchUiState = SearchUiState.Idle,
    ) : WeatherUiState
}

@Immutable
internal sealed interface SearchUiState {
    data object Idle : SearchUiState

    data object Searching : SearchUiState

    data class Results(
        val locations: List<LocationUiModel>,
    ) : SearchUiState

    data object NoResults : SearchUiState

    data object Failed : SearchUiState
}

internal enum class WeatherErrorReason {
    NETWORK,
    RATE_LIMITED,
    LOCATION_UNAVAILABLE,
    MALFORMED_DATA,
    SERVER,
    UNKNOWN,
}
