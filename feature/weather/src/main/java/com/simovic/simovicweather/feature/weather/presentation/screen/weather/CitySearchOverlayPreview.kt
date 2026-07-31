package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Composable
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppPreview
import com.simovic.simovicweather.feature.base.presentation.compose.composable.PreviewAppThemes
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation

@PreviewAppThemes
@Composable
private fun CitySearchIdlePreview() {
    CitySearchPreview(LocationSearchUiState(isVisible = true))
}

@PreviewAppThemes
@Composable
private fun CitySearchShortQueryPreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            query = "Sa",
            status = LocationSearchStatus.QueryTooShort,
        ),
    )
}

@PreviewAppThemes
@Composable
private fun CitySearchLoadingPreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            query = SAMPLE_QUERY,
            status = LocationSearchStatus.Searching,
        ),
    )
}

@PreviewAppThemes
@Composable
private fun CitySearchResultsPreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            query = SAMPLE_QUERY,
            status = LocationSearchStatus.Results(listOf(sampleLocationUiModel())),
        ),
    )
}

@PreviewAppThemes
@Composable
private fun CitySearchNoResultsPreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            query = "Missing",
            status = LocationSearchStatus.NoResults,
        ),
    )
}

@PreviewAppThemes
@Composable
private fun CitySearchFailurePreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            query = SAMPLE_QUERY,
            status = LocationSearchStatus.Failed(WeatherErrorReason.NETWORK),
        ),
    )
}

@PreviewAppThemes
@Composable
private fun CitySearchPermissionDeniedPreview() {
    CitySearchPreview(
        LocationSearchUiState(
            isVisible = true,
            isLocationPermissionDenied = true,
        ),
    )
}

@Composable
private fun CitySearchPreview(search: LocationSearchUiState) {
    AppPreview {
        CitySearchOverlay(
            search = search,
            shouldRequestFocus = false,
            onQueryChange = {},
            onSearchClear = {},
            onSearchClose = {},
            onRetry = {},
            onLocationSelect = {},
            onUseCurrentLocation = {},
        )
    }
}

private fun sampleLocationUiModel(): LocationUiModel {
    val location =
        WeatherLocation(
            id = 1,
            name = SAMPLE_QUERY,
            region = "Federation of Bosnia and Herzegovina",
            country = "Bosnia and Herzegovina",
            coordinates = Coordinates(SAMPLE_LATITUDE, SAMPLE_LONGITUDE),
        )
    return LocationUiModel(
        label = "Sarajevo, Federation of Bosnia and Herzegovina, Bosnia and Herzegovina",
        location = location,
    )
}

private const val SAMPLE_QUERY = "Sarajevo"
private const val SAMPLE_LATITUDE = 43.8563
private const val SAMPLE_LONGITUDE = 18.4131
