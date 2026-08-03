package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForCurrentLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.SearchLocationsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
// This screen-level orchestrator intentionally exposes one event per user action.
@Suppress("TooManyFunctions")
internal class WeatherViewModel(
    private val getCurrentWeather: GetWeatherForCurrentLocationUseCase,
    private val getWeatherForLocation: GetWeatherForLocationUseCase,
    private val searchLocations: SearchLocationsUseCase,
    private val presentationMapper: WeatherPresentationMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(WeatherScreenUiState())
    val uiState: StateFlow<WeatherScreenUiState> = mutableUiState.asStateFlow()

    private val searchRequests = MutableStateFlow(SearchRequest())
    private var forecastJob: Job? = null

    // Prevent stale results from updating state when dependencies ignore cancellation.
    private var forecastRequestId = 0L
    private var searchRequestId = 0L

    init {
        viewModelScope.launch {
            searchRequests
                .flatMapLatest { request ->
                    flow {
                        if (!request.isRetry) delay(SEARCH_DEBOUNCE_MILLIS)
                        val query = request.query.trim()
                        when {
                            query.isEmpty() -> {
                                emit(LocationSearchStatus.Idle)
                            }
                            query.length < MINIMUM_QUERY_LENGTH -> {
                                emit(LocationSearchStatus.QueryTooShort)
                            }
                            else -> {
                                emit(LocationSearchStatus.Searching)
                                val language = Locale.getDefault().language
                                val status =
                                    when (val result = searchLocations(query, language)) {
                                        is Result.Success -> {
                                            val locations = result.value.map { presentationMapper.toLocationUiModel(it) }
                                            if (locations.isEmpty()) {
                                                LocationSearchStatus.NoResults
                                            } else {
                                                LocationSearchStatus.Results(locations)
                                            }
                                        }
                                        is Result.Failure -> {
                                            LocationSearchStatus.Failed(presentationMapper.toError(result.reason))
                                        }
                                    }
                                emit(status)
                            }
                        }
                    }
                }.collect { status -> mutableUiState.updateSearch { state -> state.copy(status = status) } }
        }
    }

    fun onInitialLocationPermissionChecked(isGranted: Boolean) {
        if (isGranted) loadCurrentLocationWeather() else openLocationSearch()
    }

    fun onLocationPermissionRequestResult(granted: Boolean) {
        if (granted) loadCurrentLocationWeather() else openLocationSearch(permissionDenied = true)
    }

    fun onCurrentLocationRequested() = loadCurrentLocationWeather()

    fun onSearchOpened() {
        openLocationSearch()
    }

    fun onSearchClosed() {
        searchRequests.value = SearchRequest(id = ++searchRequestId)
        mutableUiState.updateSearch { LocationSearchUiState() }
    }

    fun onSearchQueryChanged(query: String) {
        val status =
            when {
                query.isBlank() -> LocationSearchStatus.Idle
                query.trim().length < MINIMUM_QUERY_LENGTH -> LocationSearchStatus.QueryTooShort
                else -> LocationSearchStatus.Idle
            }
        mutableUiState.updateSearch { state -> state.copy(query = query, status = status) }
        searchRequests.value = SearchRequest(query, ++searchRequestId)
    }

    fun retrySearch() {
        val query = mutableUiState.value.search.query
        if (query.trim().length >= MINIMUM_QUERY_LENGTH) {
            searchRequests.value = SearchRequest(query, ++searchRequestId, isRetry = true)
        }
    }

    fun onLocationSelected(location: LocationUiModel) {
        onSearchClosed()
        val target = ForecastTarget.SelectedLocation(location)
        mutableUiState.update { state -> state.copy(target = target) }
        loadForecast(target)
    }

    fun retry() = loadForecast(mutableUiState.value.target)

    private fun loadCurrentLocationWeather() {
        onSearchClosed()
        val target = ForecastTarget.CurrentLocation
        mutableUiState.update { state -> state.copy(target = target) }
        loadForecast(target)
    }

    private fun openLocationSearch(permissionDenied: Boolean = false) {
        mutableUiState.updateSearch { state ->
            state.copy(
                isVisible = true,
                isLocationPermissionDenied = permissionDenied,
            )
        }
    }

    private fun loadForecast(requestedTarget: ForecastTarget) {
        forecastJob?.cancel()
        val activeRequestId = ++forecastRequestId
        val previousForecast = mutableUiState.value.forecast
        mutableUiState.update { state -> state.copy(forecast = WeatherUiState.Loading) }
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

    private suspend fun getForecast(target: ForecastTarget): Result<WeatherForecast> =
        when (target) {
            ForecastTarget.CurrentLocation -> getCurrentWeather()
            is ForecastTarget.SelectedLocation -> getWeatherForLocation(target.location.location)
        }

    private data class SearchRequest(
        val query: String = "",
        val id: Long = 0,
        val isRetry: Boolean = false,
    )

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
                    state.withPermissionDeniedSearch().copy(forecast = previousForecast)
                } else {
                    state.copy(forecast = presentationMapper.toWeatherErrorUiState(result.reason))
                }
            }
        }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
        const val MINIMUM_QUERY_LENGTH = 3
    }
}

private fun WeatherScreenUiState.withPermissionDeniedSearch(): WeatherScreenUiState =
    copy(
        search =
            search.copy(
                isVisible = true,
                isLocationPermissionDenied = true,
            ),
    )

private fun MutableStateFlow<WeatherScreenUiState>.updateSearch(transform: (LocationSearchUiState) -> LocationSearchUiState) {
    value = value.copy(search = transform(value.search))
}
