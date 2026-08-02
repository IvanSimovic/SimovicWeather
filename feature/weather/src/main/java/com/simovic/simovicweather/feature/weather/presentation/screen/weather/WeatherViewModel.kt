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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
// This screen-level orchestrator intentionally exposes one event per user action.
@Suppress("TooManyFunctions")
internal class WeatherViewModel(
    private val getCurrentWeather: GetWeatherForCurrentLocationUseCase,
    private val getWeatherForLocation: GetWeatherForLocationUseCase,
    private val searchLocations: SearchLocationsUseCase,
    private val presentationMapper: WeatherPresentationMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial())
    val uiState: StateFlow<WeatherUiState> = mutableUiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var lastLoad: suspend () -> Result<WeatherForecast> = getCurrentWeather::invoke

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    flow {
                        if (query.trim().length < MINIMUM_QUERY_LENGTH) {
                            emit(SearchUiState.Idle)
                        } else {
                            emit(SearchUiState.Searching)
                            val language = Locale.getDefault().language
                            val state =
                                when (val result = searchLocations(query, language)) {
                                    is Result.Success -> {
                                        val locations = result.value.map(presentationMapper::toLocationUiModel)
                                        if (locations.isEmpty()) SearchUiState.NoResults else SearchUiState.Results(locations)
                                    }
                                    is Result.Failure -> {
                                        SearchUiState.Failed
                                    }
                                }
                            emit(state)
                        }
                    }
                }.collect(::updateSearch)
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) loadCurrentLocation() else mutableUiState.value = WeatherUiState.PermissionRequired()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onLocationSelected(location: LocationUiModel) {
        searchQuery.value = ""
        load { getWeatherForLocation(location.location) }
    }

    fun retry() = load(lastLoad)

    fun refresh() = retry()

    private fun loadCurrentLocation() = load(getCurrentWeather::invoke)

    private fun load(block: suspend () -> Result<WeatherForecast>) {
        lastLoad = block
        val search = mutableUiState.value.search
        mutableUiState.value = WeatherUiState.Loading(search)
        viewModelScope.launch {
            mutableUiState.value =
                when (val result = block()) {
                    is Result.Success -> WeatherUiState.Content(presentationMapper.toUiModel(result.value), search)
                    is Result.Failure -> result.reason.toUiState(search)
                }
        }
    }

    private fun AppFailure.toUiState(search: SearchUiState): WeatherUiState =
        if (this == AppFailure.PermissionDenied) {
            WeatherUiState.PermissionRequired(search)
        } else {
            WeatherUiState.Error(
                reason = presentationMapper.toError(this),
                canRetry = this !is AppFailure.MalformedData,
                search = search,
            )
        }

    private fun updateSearch(search: SearchUiState) {
        mutableUiState.value =
            when (val state = mutableUiState.value) {
                is WeatherUiState.Initial -> state.copy(search = search)
                is WeatherUiState.Loading -> state.copy(search = search)
                is WeatherUiState.Content -> state.copy(search = search)
                is WeatherUiState.PermissionRequired -> state.copy(search = search)
                is WeatherUiState.Error -> state.copy(search = search)
            }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
        const val MINIMUM_QUERY_LENGTH = 3
    }
}
