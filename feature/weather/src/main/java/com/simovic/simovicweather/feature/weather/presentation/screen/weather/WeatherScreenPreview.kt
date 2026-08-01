package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.simovic.simovicweather.feature.base.presentation.compose.composable.AppPreview
import com.simovic.simovicweather.feature.base.presentation.compose.composable.PreviewAppThemes
import com.simovic.simovicweather.feature.weather.R

@PreviewAppThemes
@Composable
private fun WeatherInitialPreview() {
    WeatherPreview(uiState = WeatherUiState.Initial)
}

@PreviewAppThemes
@Composable
private fun WeatherLoadingPreview() {
    WeatherPreview(uiState = WeatherUiState.Loading)
}

@PreviewAppThemes
@Composable
private fun WeatherContentPreview() {
    WeatherPreview(uiState = WeatherUiState.Content(sampleWeather()))
}

@PreviewAppThemes
@Composable
private fun WeatherRefreshingPreview() {
    WeatherPreview(
        uiState = WeatherUiState.Content(sampleWeather()),
        refresh = RefreshUiState.Refreshing,
    )
}

@Preview(
    name = "Narrow forecast",
    widthDp = 320,
    heightDp = 720,
    showBackground = true,
)
@Preview(
    name = "Large text forecast",
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f,
    showBackground = true,
)
@Composable
private fun NarrowWeatherContentPreview() {
    WeatherPreview(uiState = WeatherUiState.Content(sampleWeather()))
}

@PreviewAppThemes
@Composable
private fun LongLocationWeatherContentPreview() {
    WeatherPreview(
        uiState =
            WeatherUiState.Content(
                sampleWeather(
                    locationName = "Llanfairpwllgwyngyll, Isle of Anglesey, United Kingdom",
                ),
            ),
    )
}

@PreviewAppThemes
@Composable
private fun WeatherRetryableErrorPreview() {
    WeatherPreview(
        uiState = WeatherUiState.Error(WeatherErrorReason.NETWORK, canRetry = true),
    )
}

@PreviewAppThemes
@Composable
private fun WeatherNonRetryableErrorPreview() {
    WeatherPreview(
        uiState = WeatherUiState.Error(WeatherErrorReason.MALFORMED_DATA, canRetry = false),
    )
}

@Composable
private fun WeatherPreview(
    uiState: WeatherUiState,
    refresh: RefreshUiState = RefreshUiState.Idle,
) {
    AppPreview {
        WeatherScreenContent(
            uiState = WeatherScreenUiState(forecast = uiState, refresh = refresh),
            onUseCurrentLocation = {},
            onRetry = {},
            onSearchOpen = {},
            onSearchClose = {},
            onSearchQueryChange = {},
            onSearchClear = {},
            onSearchRetry = {},
            onLocationSelect = {},
            onRefresh = {},
        )
    }
}

private fun sampleWeather(locationName: String = "Sarajevo, Bosnia and Herzegovina") =
    WeatherUiModel(
        locationName = locationName,
        updatedAt = "12:00 PM",
        current =
            CurrentWeatherUiModel(
                temperatureCelsius = 23,
                apparentTemperatureCelsius = 24,
                humidityPercent = 55,
                condition = WeatherConditionUiModel(R.string.weather_condition_partly_cloudy, WeatherIcon.PARTLY_CLOUDY),
                precipitationMillimeters = 0.5,
                pressureHectopascals = 1015,
                windSpeedKilometersPerHour = 11,
            ),
        todayMinimumTemperatureCelsius = 15,
        todayMaximumTemperatureCelsius = 25,
        days =
            List(SAMPLE_FORECAST_DAY_COUNT) { index ->
                DailyWeatherUiModel(
                    date = "Sat, Aug ${index + 1}",
                    isToday = index == 0,
                    minimumTemperatureCelsius = 15 + index,
                    maximumTemperatureCelsius = 25 + index,
                    precipitationProbabilityPercent = 10,
                    condition =
                        WeatherConditionUiModel(
                            R.string.weather_condition_partly_cloudy,
                            WeatherIcon.PARTLY_CLOUDY,
                        ),
                )
            },
    )

private const val SAMPLE_FORECAST_DAY_COUNT = 7
