package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.DailyWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForCurrentLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.SearchLocationsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @get:Rule
    internal val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `opening and closing search preserves forecast and clears transient search state`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()

            viewModel.onSearchOpened()
            viewModel.onSearchQueryChanged("Sarajevo")
            viewModel.onSearchClosed()

            assertEquals(WeatherUiState.Initial, viewModel.uiState.value.forecast)
            assertEquals(LocationSearchUiState(), viewModel.uiState.value.search)
        }

    @Test
    fun `query shorter than three characters does not search`() =
        runTest(mainDispatcherRule.dispatcher) {
            val searchRepository = FakeLocationRepository()
            val viewModel = createViewModel(searchRepository = searchRepository)

            viewModel.onSearchQueryChanged("Sa")
            advanceUntilIdle()

            assertTrue(searchRepository.queries.isEmpty())
            assertEquals(LocationSearchStatus.QueryTooShort, viewModel.uiState.value.search.status)
        }

    @Test
    fun `valid query waits four hundred milliseconds before searching`() =
        runTest(mainDispatcherRule.dispatcher) {
            val searchRepository = FakeLocationRepository()
            val viewModel = createViewModel(searchRepository = searchRepository)

            viewModel.onSearchQueryChanged("Sarajevo")
            runCurrent()
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
            runCurrent()
            assertTrue(searchRepository.queries.isEmpty())

            advanceTimeBy(1)
            runCurrent()

            assertEquals(listOf("Sarajevo"), searchRepository.queries)
        }

    @Test
    fun `new query cancels old search and only exposes latest results`() =
        runTest(mainDispatcherRule.dispatcher) {
            val firstResult = location(id = 1, name = "Old result")
            val secondResult = location(id = 2, name = "New result")
            val searchRepository =
                FakeLocationRepository(
                    handler = { query ->
                        if (query == "First") {
                            delay(1_000)
                            Result.Success(listOf(firstResult))
                        } else {
                            Result.Success(listOf(secondResult))
                        }
                    },
                )
            val viewModel = createViewModel(searchRepository = searchRepository)

            viewModel.onSearchQueryChanged("First")
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
            runCurrent()
            viewModel.onSearchQueryChanged("Second")
            advanceUntilIdle()

            val results = viewModel.uiState.value.search.status
            assertTrue(results is LocationSearchStatus.Results)
            assertEquals(listOf(secondResult), (results as LocationSearchStatus.Results).locations.map(LocationUiModel::location))
        }

    @Test
    fun `empty result and failure have distinct statuses`() =
        runTest(mainDispatcherRule.dispatcher) {
            val searchRepository = FakeLocationRepository()
            val viewModel = createViewModel(searchRepository = searchRepository)

            viewModel.onSearchQueryChanged("Missing")
            advanceUntilIdle()
            assertEquals(LocationSearchStatus.NoResults, viewModel.uiState.value.search.status)

            searchRepository.result = Result.Failure(AppFailure.Connectivity())
            viewModel.onSearchQueryChanged("Offline")
            advanceUntilIdle()

            assertEquals(
                LocationSearchStatus.Failed(WeatherErrorReason.NETWORK),
                viewModel.uiState.value.search.status,
            )
        }

    @Test
    fun `retry runs unchanged failed query again without debounce`() =
        runTest(mainDispatcherRule.dispatcher) {
            val searchRepository = FakeLocationRepository(result = Result.Failure(AppFailure.Server(code = 500)))
            val viewModel = createViewModel(searchRepository = searchRepository)

            viewModel.onSearchQueryChanged("Sarajevo")
            advanceUntilIdle()
            searchRepository.result = Result.Success(listOf(location()))

            viewModel.retrySearch()
            runCurrent()

            assertEquals(listOf("Sarajevo", "Sarajevo"), searchRepository.queries)
            assertTrue(viewModel.uiState.value.search.status is LocationSearchStatus.Results)
        }

    @Test
    fun `selecting location closes search and loads selected forecast`() =
        runTest(mainDispatcherRule.dispatcher) {
            val weatherRepository = FakeWeatherRepository()
            val selectedLocation = location(id = 2, name = "Mostar")
            val viewModel = createViewModel(weatherRepository = weatherRepository)

            viewModel.onSearchOpened()
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()

            val target = viewModel.uiState.value.target as ForecastTarget.SelectedLocation
            assertFalse(viewModel.uiState.value.search.isVisible)
            assertEquals(selectedLocation, target.location.location)
            assertEquals(listOf(selectedLocation), weatherRepository.locations)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
        }

    @Test
    fun `missing permission opens city picker without denial feedback`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()

            viewModel.onInitialLocationPermissionChecked(isGranted = false)

            assertTrue(viewModel.uiState.value.search.isVisible)
            assertFalse(viewModel.uiState.value.search.isLocationPermissionDenied)
            assertEquals(WeatherUiState.Initial, viewModel.uiState.value.forecast)
        }

    @Test
    fun `refused permission keeps city picker open and preserves forecast`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            viewModel.onLocationSelected(LocationUiModel("Mostar", location(name = "Mostar")))
            advanceUntilIdle()

            viewModel.onSearchOpened()
            viewModel.onLocationPermissionRequestResult(granted = false)

            assertTrue(viewModel.uiState.value.search.isVisible)
            assertTrue(viewModel.uiState.value.search.isLocationPermissionDenied)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
        }

    @Test
    fun `granted permission request closes picker and loads current location`() =
        runTest(mainDispatcherRule.dispatcher) {
            val currentLocation = location(id = 3, name = "Current")
            val weatherRepository = FakeWeatherRepository()
            val viewModel =
                createViewModel(
                    weatherRepository = weatherRepository,
                    deviceLocationRepository = FakeDeviceLocationRepository(currentLocation),
                )
            viewModel.onSearchOpened()

            viewModel.onLocationPermissionRequestResult(granted = true)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.search.isVisible)
            assertEquals(ForecastTarget.CurrentLocation, viewModel.uiState.value.target)
            assertEquals(listOf(currentLocation), weatherRepository.locations)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
        }

    @Test
    fun `available permission at startup loads current location`() =
        runTest(mainDispatcherRule.dispatcher) {
            val currentLocation = location(id = 3, name = "Current")
            val weatherRepository = FakeWeatherRepository()
            val viewModel =
                createViewModel(
                    weatherRepository = weatherRepository,
                    deviceLocationRepository = FakeDeviceLocationRepository(currentLocation),
                )

            viewModel.onInitialLocationPermissionChecked(isGranted = true)
            advanceUntilIdle()

            assertEquals(listOf(currentLocation), weatherRepository.locations)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
        }

    @Test
    fun `current location permission failure opens city picker and preserves forecast`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel =
                createViewModel(
                    deviceLocationRepository = PermissionDeniedDeviceLocationRepository(),
                )
            viewModel.onLocationSelected(LocationUiModel("Mostar", location(name = "Mostar")))
            advanceUntilIdle()
            val existingForecast = viewModel.uiState.value.forecast

            viewModel.onCurrentLocationRequested()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.search.isVisible)
            assertTrue(viewModel.uiState.value.search.isLocationPermissionDenied)
            assertEquals(existingForecast, viewModel.uiState.value.forecast)
        }

    @Test
    fun `retry reloads the selected location`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { requestedLocation, call ->
                    if (call == 1) {
                        Result.Failure(AppFailure.Connectivity())
                    } else {
                        Result.Success(forecast(requestedLocation))
                    }
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)

            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()
            viewModel.retry()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
            assertEquals(listOf(selectedLocation, selectedLocation), weatherRepository.locations)
        }

    @Test
    fun `obsolete request cannot replace a newer selected location`() =
        runTest(mainDispatcherRule.dispatcher) {
            val firstResult = CompletableDeferred<Result<WeatherForecast>>()
            val firstLocation = location(id = 1, name = "Sarajevo")
            val secondLocation = location(id = 2, name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { requestedLocation, call ->
                    if (call == 1) {
                        withContext(NonCancellable) { firstResult.await() }
                    } else {
                        Result.Success(forecast(requestedLocation))
                    }
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)

            viewModel.onLocationSelected(LocationUiModel("Sarajevo", firstLocation))
            runCurrent()
            viewModel.onLocationSelected(LocationUiModel("Mostar", secondLocation))
            runCurrent()
            firstResult.complete(Result.Success(forecast(firstLocation)))
            advanceUntilIdle()

            val content = viewModel.uiState.value.forecast as WeatherUiState.Content
            assertEquals("Mostar, Bosnia and Herzegovina", content.weather.locationName)
            assertEquals(listOf(firstLocation, secondLocation), weatherRepository.locations)
        }

    private fun createViewModel(
        searchRepository: FakeLocationRepository = FakeLocationRepository(),
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository(),
        deviceLocationRepository: DeviceLocationRepository = FakeDeviceLocationRepository(),
    ) = WeatherViewModel(
        getCurrentWeather =
            GetWeatherForCurrentLocationUseCase(
                deviceLocationRepository = deviceLocationRepository,
                weatherRepository = weatherRepository,
            ),
        getWeatherForLocation = GetWeatherForLocationUseCase(weatherRepository),
        searchLocations = SearchLocationsUseCase(searchRepository),
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

    private class FakeWeatherRepository(
        private val handler: suspend (WeatherLocation, Int) -> Result<WeatherForecast> =
            { requestedLocation, _ -> Result.Success(forecast(requestedLocation)) },
    ) : WeatherRepository {
        val locations = mutableListOf<WeatherLocation>()

        override suspend fun getForecast(location: WeatherLocation): Result<WeatherForecast> {
            locations += location
            return handler(location, locations.size)
        }
    }

    private class FakeDeviceLocationRepository(
        private val location: WeatherLocation = location(),
    ) : DeviceLocationRepository {
        override suspend fun getCurrentLocation(): Result<WeatherLocation> = Result.Success(location)
    }

    private class PermissionDeniedDeviceLocationRepository : DeviceLocationRepository {
        override suspend fun getCurrentLocation(): Result<WeatherLocation> =
            Result.Failure(AppFailure.PermissionDenied)
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

        fun forecast(location: WeatherLocation) =
            WeatherForecast(
                location = location,
                current =
                    CurrentWeather(
                        time = LocalDateTime.of(2026, 7, 31, 12, 0),
                        temperature = 25.0,
                        apparentTemperature = 26.0,
                        humidity = 50,
                        precipitation = 0.0,
                        weatherCode = 1,
                        pressure = 1_015.0,
                        windSpeed = 10.0,
                    ),
                days =
                    listOf(
                        DailyWeather(
                            date = LocalDate.of(2026, 7, 31),
                            weatherCode = 1,
                            minTemperature = 15.0,
                            maxTemperature = 26.0,
                            precipitationProbability = 10,
                        ),
                    ),
            )
    }
}
