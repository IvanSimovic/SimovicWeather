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
    private var lastLoad: suspend () -> Result<WeatherForecast> = getCurrentWeather::invoke
    private var forecastJob: Job? = null
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
                                            if (locations.isEmpty()) LocationSearchStatus.NoResults else LocationSearchStatus.Results(locations)
                                        }
                                        is Result.Failure -> {
                                            LocationSearchStatus.Failed(presentationMapper.toError(result.reason))
                                        }
                                    }
                                emit(status)
                            }
                        }
                    }
                }.collect { status -> updateSearch { state -> state.copy(status = status) } }
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
        updateSearch { LocationSearchUiState() }
    }

    fun onSearchQueryChanged(query: String) {
        val status =
            when {
                query.isBlank() -> LocationSearchStatus.Idle
                query.trim().length < MINIMUM_QUERY_LENGTH -> LocationSearchStatus.QueryTooShort
                else -> LocationSearchStatus.Idle
            }
        updateSearch { state -> state.copy(query = query, status = status) }
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
        load { getWeatherForLocation(location.location) }
    }

    fun retry() = load(lastLoad)

    private fun loadCurrentLocationWeather() {
        onSearchClosed()
        load(getCurrentWeather::invoke)
    }

    private fun openLocationSearch(permissionDenied: Boolean = false) {
        updateSearch { state ->
            state.copy(
                isVisible = true,
                isLocationPermissionDenied = permissionDenied,
            )
        }
    }

    private fun load(block: suspend () -> Result<WeatherForecast>) {
        lastLoad = block
        forecastJob?.cancel()
        val previousForecast = mutableUiState.value.forecast
        mutableUiState.value = mutableUiState.value.copy(forecast = WeatherUiState.Loading)
        forecastJob =
            viewModelScope.launch {
                val forecast =
                    when (val result = block()) {
                        is Result.Success -> {
                            WeatherUiState.Content(presentationMapper.toUiModel(result.value))
                        }
                        is Result.Failure -> {
                            if (result.reason == AppFailure.PermissionDenied) {
                                updateSearch { state ->
                                    state.copy(
                                        isVisible = true,
                                        isLocationPermissionDenied = true,
                                    )
                                }
                                previousForecast
                            } else {
                                presentationMapper.toWeatherErrorUiState(result.reason)
                            }
                        }
                    }
                mutableUiState.value = mutableUiState.value.copy(forecast = forecast)
            }
    }

    private fun updateSearch(transform: (LocationSearchUiState) -> LocationSearchUiState) {
        mutableUiState.value = mutableUiState.value.copy(search = transform(mutableUiState.value.search))
    }

    private data class SearchRequest(
        val query: String = "",
        val id: Long = 0,
        val isRetry: Boolean = false,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
        const val MINIMUM_QUERY_LENGTH = 3
    }
}
