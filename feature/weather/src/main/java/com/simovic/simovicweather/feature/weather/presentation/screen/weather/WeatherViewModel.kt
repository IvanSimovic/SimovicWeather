package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForCurrentLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForLocationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// This screen-level orchestrator intentionally exposes one event per user action.
@Suppress("TooManyFunctions")
internal class WeatherViewModel(
    private val getCurrentWeather: GetWeatherForCurrentLocationUseCase,
    private val getWeatherForLocation: GetWeatherForLocationUseCase,
    private val presentationMapper: WeatherPresentationMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(WeatherScreenUiState())
    val uiState: StateFlow<WeatherScreenUiState> = mutableUiState.asStateFlow()

    private var forecastJob: Job? = null

    // Prevent stale results from updating state when dependencies ignore cancellation.
    private var forecastRequestId = 0L

    fun onCurrentLocationRequested() = loadCurrentLocation()

    fun onLocationSearchRequestHandled() {
        mutableUiState.update { state -> state.copy(shouldOpenLocationSearch = false) }
    }

    fun onLocationSelected(location: WeatherLocation) {
        val target = ForecastTarget.SelectedLocation(presentationMapper.toLocationUiModel(location))
        mutableUiState.update { state -> state.copy(target = target) }
        loadForecast(target)
    }

    fun retry() = loadForecast(mutableUiState.value.target)

    private fun loadCurrentLocation() {
        val target = ForecastTarget.CurrentLocation
        mutableUiState.update { state -> state.copy(target = target) }
        loadForecast(target)
    }

    fun refresh() {
        val state = mutableUiState.value
        if (
            state.forecast is WeatherUiState.Content &&
            state.refresh != RefreshUiState.Refreshing &&
            !state.shouldOpenLocationSearch
        ) {
            refreshForecast(state.target)
        }
    }

    private fun loadForecast(requestedTarget: ForecastTarget) {
        forecastJob?.cancel()
        val activeRequestId = ++forecastRequestId
        val previousForecast = mutableUiState.value.forecast
        mutableUiState.update { state ->
            state.copy(forecast = WeatherUiState.Loading, refresh = RefreshUiState.Idle)
        }
        forecastJob =
            viewModelScope.launch {
                val result = getForecast(requestedTarget)
                if (activeRequestId == forecastRequestId) {
                    mutableUiState.update { state ->
                        stateAfterLoad(state, result, previousForecast)
                    }
                }
            }
    }

    private fun refreshForecast(requestedTarget: ForecastTarget) {
        forecastJob?.cancel()
        val activeRequestId = ++forecastRequestId
        mutableUiState.update { state -> state.copy(refresh = RefreshUiState.Refreshing) }
        forecastJob =
            viewModelScope.launch {
                val result = getForecast(requestedTarget)
                if (activeRequestId == forecastRequestId) {
                    mutableUiState.update { state ->
                        stateAfterRefresh(state, result)
                    }
                }
            }
    }

    private suspend fun getForecast(target: ForecastTarget): Result<WeatherForecast> =
        when (target) {
            ForecastTarget.CurrentLocation -> getCurrentWeather()
            is ForecastTarget.SelectedLocation -> getWeatherForLocation(target.location.location)
        }

    private fun stateAfterLoad(
        state: WeatherScreenUiState,
        result: Result<WeatherForecast>,
        previousForecast: WeatherUiState,
    ): WeatherScreenUiState =
        when (result) {
            is Result.Success -> {
                val weather = presentationMapper.toUiModel(result.value)
                state.copy(forecast = WeatherUiState.Content(weather))
            }
            is Result.Failure -> {
                if (result.reason == AppFailure.PermissionDenied) {
                    state.copy(
                        forecast = previousForecast,
                        shouldOpenLocationSearch = true,
                    )
                } else {
                    state.copy(forecast = presentationMapper.toWeatherErrorUiState(result.reason))
                }
            }
        }

    private fun stateAfterRefresh(
        state: WeatherScreenUiState,
        result: Result<WeatherForecast>,
    ): WeatherScreenUiState =
        when (result) {
            is Result.Success -> {
                val refreshedForecast = WeatherUiState.Content(presentationMapper.toUiModel(result.value))
                if (state.forecast == refreshedForecast) {
                    state.copy(refresh = RefreshUiState.UpToDate)
                } else {
                    state.copy(forecast = refreshedForecast, refresh = RefreshUiState.Idle)
                }
            }
            is Result.Failure -> {
                if (result.reason == AppFailure.PermissionDenied) {
                    state.copy(
                        refresh = RefreshUiState.Idle,
                        shouldOpenLocationSearch = true,
                    )
                } else {
                    state.copy(refresh = RefreshUiState.Failed(presentationMapper.toError(result.reason)))
                }
            }
        }
}
