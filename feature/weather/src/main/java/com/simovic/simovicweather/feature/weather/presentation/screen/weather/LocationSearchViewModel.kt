package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.usecase.SearchLocationsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class LocationSearchViewModel(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val presentationMapper: WeatherPresentationMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LocationSearchUiState())
    val uiState: StateFlow<LocationSearchUiState> = mutableUiState.asStateFlow()

    private val searchRequests =
        MutableSharedFlow<SearchRequest>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    init {
        viewModelScope.launch {
            searchRequests
                .flatMapLatest(::createStatusFlow)
                .collect { status -> mutableUiState.update { state -> state.copy(status = status) } }
        }
    }

    fun onQueryChanged(query: String) {
        mutableUiState.update { state -> state.copy(query = query) }
        searchRequests.tryEmit(SearchRequest(query, shouldDebounce = true))
    }

    fun retry() {
        val query = mutableUiState.value.query
        if (query.trim().length >= MINIMUM_QUERY_LENGTH) {
            searchRequests.tryEmit(SearchRequest(query, shouldDebounce = false))
        }
    }

    fun onLocationPermissionDenied() {
        mutableUiState.update { state -> state.copy(isLocationPermissionDenied = true) }
    }

    private fun createStatusFlow(request: SearchRequest) =
        flow {
            val normalizedQuery = request.query.trim()
            when {
                normalizedQuery.isEmpty() -> {
                    emit(LocationSearchStatus.Idle)
                }
                normalizedQuery.length < MINIMUM_QUERY_LENGTH -> {
                    emit(LocationSearchStatus.QueryTooShort)
                }
                else -> {
                    if (request.shouldDebounce) {
                        emit(LocationSearchStatus.Idle)
                        delay(SEARCH_DEBOUNCE_MILLIS)
                    }
                    emit(LocationSearchStatus.Searching)
                    emit(searchLocations(normalizedQuery))
                }
            }
        }

    private suspend fun searchLocations(query: String): LocationSearchStatus =
        when (val result = searchLocationsUseCase(query, Locale.getDefault().language)) {
            is Result.Success -> {
                val locations = result.value.map(presentationMapper::toLocationUiModel)
                if (locations.isEmpty()) LocationSearchStatus.NoResults else LocationSearchStatus.Results(locations)
            }
            is Result.Failure -> {
                LocationSearchStatus.Failed(presentationMapper.toError(result.reason))
            }
        }

    private data class SearchRequest(
        val query: String,
        val shouldDebounce: Boolean,
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
        const val MINIMUM_QUERY_LENGTH = 3
    }
}
