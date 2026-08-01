package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository
import com.simovic.simovicweather.feature.weather.domain.usecase.SearchLocationsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSearchViewModelTest {
    @get:Rule
    internal val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `query shorter than three characters does not search`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("Sa")
            runCurrent()

            assertTrue(repository.queries.isEmpty())
            assertEquals(LocationSearchStatus.QueryTooShort, viewModel.uiState.value.status)
        }

    @Test
    fun `valid query waits four hundred milliseconds before searching`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("Sarajevo")
            runCurrent()
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
            runCurrent()
            assertTrue(repository.queries.isEmpty())

            advanceTimeBy(1)
            runCurrent()

            assertEquals(listOf("Sarajevo"), repository.queries)
        }

    @Test
    fun `new query cancels old search and exposes only latest results`() =
        runTest(mainDispatcherRule.dispatcher) {
            val oldLocation = location(id = 1, name = "Old result")
            val newLocation = location(id = 2, name = "New result")
            val repository =
                FakeLocationRepository(
                    handler = { query ->
                        if (query == "First") {
                            delay(1_000)
                            Result.Success(listOf(oldLocation))
                        } else {
                            Result.Success(listOf(newLocation))
                        }
                    },
                )
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("First")
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
            runCurrent()
            viewModel.onQueryChanged("Second")
            advanceUntilIdle()

            val results = viewModel.uiState.value.status as LocationSearchStatus.Results
            assertEquals(listOf(newLocation), results.locations.map(LocationUiModel::location))
        }

    @Test
    fun `empty result and failure have distinct statuses`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("Missing")
            advanceUntilIdle()
            assertEquals(LocationSearchStatus.NoResults, viewModel.uiState.value.status)

            repository.result = Result.Failure(AppFailure.Connectivity())
            viewModel.onQueryChanged("Offline")
            advanceUntilIdle()
            assertEquals(
                LocationSearchStatus.Failed(WeatherErrorReason.NETWORK),
                viewModel.uiState.value.status,
            )
        }

    @Test
    fun `retry runs unchanged failed query immediately`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Failure(AppFailure.Server(code = 500)))
            val viewModel = createViewModel(repository)
            runCurrent()
            viewModel.onQueryChanged("Sarajevo")
            advanceUntilIdle()
            repository.result = Result.Success(listOf(location()))

            viewModel.retry()
            runCurrent()

            assertEquals(listOf("Sarajevo", "Sarajevo"), repository.queries)
            assertTrue(viewModel.uiState.value.status is LocationSearchStatus.Results)
        }

    @Test
    fun `clearing query resets status without searching`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Success(listOf(location())))
            val viewModel = createViewModel(repository)
            runCurrent()
            viewModel.onQueryChanged("Sarajevo")
            advanceUntilIdle()

            viewModel.onQueryChanged("")
            runCurrent()

            assertEquals("", viewModel.uiState.value.query)
            assertEquals(LocationSearchStatus.Idle, viewModel.uiState.value.status)
            assertEquals(listOf("Sarajevo"), repository.queries)
        }

    @Test
    fun `denied permission is exposed in search state`() {
        val viewModel = createViewModel(FakeLocationRepository())
        assertFalse(viewModel.uiState.value.isLocationPermissionDenied)

        viewModel.onLocationPermissionDenied()

        assertTrue(viewModel.uiState.value.isLocationPermissionDenied)
    }

    @Test
    fun `search status updates preserve denied permission state`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Success(listOf(location())))
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("Sarajevo")
            viewModel.onLocationPermissionDenied()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isLocationPermissionDenied)
            assertTrue(viewModel.uiState.value.status is LocationSearchStatus.Results)
        }

    @Test
    fun `new valid query clears previous results before debounce`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Success(listOf(location())))
            val viewModel = createViewModel(repository)
            runCurrent()
            viewModel.onQueryChanged("Sarajevo")
            advanceUntilIdle()

            viewModel.onQueryChanged("Mostar")
            runCurrent()

            assertEquals(LocationSearchStatus.Idle, viewModel.uiState.value.status)
            assertEquals(listOf("Sarajevo"), repository.queries)
        }

    @Test
    fun `rapid pending queries retain only latest query`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Success(listOf(location())))
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onQueryChanged("Sarajevo")
            viewModel.onQueryChanged("Mostar")
            advanceUntilIdle()

            assertEquals(listOf("Mostar"), repository.queries)
        }

    @Test
    fun `repeated retries execute the same request`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeLocationRepository(result = Result.Failure(AppFailure.Server(code = 500)))
            val viewModel = createViewModel(repository)
            runCurrent()
            viewModel.onQueryChanged("Sarajevo")
            advanceUntilIdle()

            viewModel.retry()
            runCurrent()
            viewModel.retry()
            runCurrent()

            assertEquals(listOf("Sarajevo", "Sarajevo", "Sarajevo"), repository.queries)
        }

    private fun createViewModel(repository: FakeLocationRepository) =
        LocationSearchViewModel(
            searchLocationsUseCase = SearchLocationsUseCase(repository),
            presentationMapper =
                WeatherPresentationMapper(
                    WeatherDateFormatter(
                        LocalizedDatePatternProvider { _, _ -> "EEE, MMM d" },
                        localeProvider = { Locale.US },
                    ),
                ),
        )

    private class FakeLocationRepository(
        var result: Result<List<WeatherLocation>> = Result.Success(emptyList()),
        private val handler: (suspend (String) -> Result<List<WeatherLocation>>)? = null,
    ) : LocationRepository {
        val queries = mutableListOf<String>()

        override suspend fun search(
            query: String,
            language: String,
        ): Result<List<WeatherLocation>> {
            queries += query
            return handler?.invoke(query) ?: result
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L

        fun location(
            id: Long = 1,
            name: String = "Sarajevo",
        ) = WeatherLocation(
            id = id,
            name = name,
            country = "Bosnia and Herzegovina",
            coordinates = Coordinates(43.8563, 18.4131),
        )
    }
}
