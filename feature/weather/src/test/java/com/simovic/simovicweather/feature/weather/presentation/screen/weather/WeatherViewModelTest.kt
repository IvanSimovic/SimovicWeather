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
import org.junit.Assert.assertNotEquals
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
            val deviceLocationRepository = FakeDeviceLocationRepository()
            deviceLocationRepository.result = Result.Failure(AppFailure.PermissionDenied)
            val viewModel =
                createViewModel(
                    deviceLocationRepository = deviceLocationRepository,
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

    @Test
    fun `refresh preserves selected forecast while request is running and replaces it on success`() =
        runTest(mainDispatcherRule.dispatcher) {
            val refreshResult = CompletableDeferred<Result<WeatherForecast>>()
            val weatherRepository =
                FakeWeatherRepository { selectedLocation, call ->
                    if (call == 1) {
                        Result.Success(forecast(selectedLocation, temperature = 20.0))
                    } else {
                        refreshResult.await()
                    }
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            val selectedLocation = location(name = "Mostar")
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()
            val previousForecast = viewModel.uiState.value.forecast

            viewModel.refresh()
            runCurrent()

            assertEquals(previousForecast, viewModel.uiState.value.forecast)
            assertEquals(RefreshUiState.Refreshing, viewModel.uiState.value.refresh)

            refreshResult.complete(Result.Success(forecast(selectedLocation, temperature = 27.0)))
            advanceUntilIdle()

            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
            val refreshed = viewModel.uiState.value.forecast as WeatherUiState.Content
            assertEquals(27, refreshed.weather.current.temperatureCelsius)
        }

    @Test
    fun `current location refresh resolves device location again`() =
        runTest(mainDispatcherRule.dispatcher) {
            val firstLocation = location(id = 1, name = "First")
            val secondLocation = location(id = 2, name = "Second")
            val deviceRepository = FakeDeviceLocationRepository(firstLocation, secondLocation)
            val weatherRepository = FakeWeatherRepository()
            val viewModel =
                createViewModel(
                    weatherRepository = weatherRepository,
                    deviceLocationRepository = deviceRepository,
                )

            viewModel.onLocationPermissionRequestResult(granted = true)
            advanceUntilIdle()
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(2, deviceRepository.calls)
            assertEquals(listOf(firstLocation, secondLocation), weatherRepository.locations)
        }

    @Test
    fun `duplicate refresh requests are ignored`() =
        runTest(mainDispatcherRule.dispatcher) {
            val refreshResult = CompletableDeferred<Result<WeatherForecast>>()
            val weatherRepository =
                FakeWeatherRepository { selectedLocation, call ->
                    if (call == 1) Result.Success(forecast(selectedLocation)) else refreshResult.await()
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            val selectedLocation = location()
            viewModel.onLocationSelected(LocationUiModel("Sarajevo", selectedLocation))
            advanceUntilIdle()

            viewModel.refresh()
            viewModel.refresh()
            runCurrent()

            assertEquals(2, weatherRepository.locations.size)
            refreshResult.complete(Result.Success(forecast(selectedLocation)))
            advanceUntilIdle()
        }

    @Test
    fun `refresh failure preserves content and another pull retries`() =
        runTest(mainDispatcherRule.dispatcher) {
            val weatherRepository =
                FakeWeatherRepository { selectedLocation, call ->
                    when (call) {
                        1 -> Result.Success(forecast(selectedLocation, temperature = 20.0))
                        2 -> Result.Failure(AppFailure.Connectivity())
                        else -> Result.Success(forecast(selectedLocation, temperature = 25.0))
                    }
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Sarajevo", location()))
            advanceUntilIdle()
            val previousForecast = viewModel.uiState.value.forecast

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(previousForecast, viewModel.uiState.value.forecast)
            assertEquals(
                RefreshUiState.Failed(WeatherErrorReason.NETWORK),
                viewModel.uiState.value.refresh,
            )

            viewModel.refresh()
            advanceUntilIdle()
            assertEquals(25, (viewModel.uiState.value.forecast as WeatherUiState.Content).weather.current.temperatureCelsius)
        }

    @Test
    fun `unchanged refresh reports up to date and preserves content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository = FakeWeatherRepository()
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()
            val previousForecast = viewModel.uiState.value.forecast

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(previousForecast, viewModel.uiState.value.forecast)
            assertEquals(RefreshUiState.UpToDate, viewModel.uiState.value.refresh)
        }

    @Test
    fun `same current time with changed current conditions updates content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { location, call ->
                    Result.Success(forecast(location, temperature = if (call == 1) 25.0 else 26.0))
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()

            viewModel.refresh()
            advanceUntilIdle()

            val weather = (viewModel.uiState.value.forecast as WeatherUiState.Content).weather
            assertEquals(26, weather.current.temperatureCelsius)
            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
        }

    @Test
    fun `same current time with changed daily forecast updates content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { location, call ->
                    Result.Success(forecast(location, dailyMaximumTemperature = if (call == 1) 26.0 else 28.0))
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()

            viewModel.refresh()
            advanceUntilIdle()

            val weather = (viewModel.uiState.value.forecast as WeatherUiState.Content).weather
            assertEquals(28, weather.days.first().maximumTemperatureCelsius)
            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
        }

    @Test
    fun `new current time updates otherwise unchanged content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { location, call ->
                    val currentTime =
                        if (call == 1) {
                            LocalDateTime.of(2026, 7, 31, 12, 0)
                        } else {
                            LocalDateTime.of(2026, 7, 31, 12, 15)
                        }
                    Result.Success(forecast(location, currentTime = currentTime))
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()
            val previousWeather = (viewModel.uiState.value.forecast as WeatherUiState.Content).weather

            viewModel.refresh()
            advanceUntilIdle()

            val refreshedWeather = (viewModel.uiState.value.forecast as WeatherUiState.Content).weather
            assertNotEquals(previousWeather.updatedAt, refreshedWeather.updatedAt)
            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
        }

    @Test
    fun `raw changes with unchanged visible values report up to date`() =
        runTest(mainDispatcherRule.dispatcher) {
            val selectedLocation = location(name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { location, call ->
                    Result.Success(forecast(location, temperature = if (call == 1) 25.1 else 25.2))
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Mostar", selectedLocation))
            advanceUntilIdle()
            val previousForecast = viewModel.uiState.value.forecast

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(previousForecast, viewModel.uiState.value.forecast)
            assertEquals(RefreshUiState.UpToDate, viewModel.uiState.value.refresh)
        }

    @Test
    fun `selected location supersedes obsolete refresh result`() =
        runTest(mainDispatcherRule.dispatcher) {
            val refreshResult = CompletableDeferred<Result<WeatherForecast>>()
            val firstLocation = location(id = 1, name = "Sarajevo")
            val secondLocation = location(id = 2, name = "Mostar")
            val weatherRepository =
                FakeWeatherRepository { requestedLocation, call ->
                    when (call) {
                        1 -> Result.Success(forecast(requestedLocation, temperature = 20.0))
                        2 -> withContext(NonCancellable) { refreshResult.await() }
                        else -> Result.Success(forecast(requestedLocation, temperature = 25.0))
                    }
                }
            val viewModel = createViewModel(weatherRepository = weatherRepository)
            viewModel.onLocationSelected(LocationUiModel("Sarajevo", firstLocation))
            advanceUntilIdle()

            viewModel.refresh()
            runCurrent()
            viewModel.onLocationSelected(LocationUiModel("Mostar", secondLocation))
            runCurrent()
            refreshResult.complete(Result.Success(forecast(firstLocation, temperature = 30.0)))
            advanceUntilIdle()

            val content = viewModel.uiState.value.forecast as WeatherUiState.Content
            assertEquals("Mostar, Bosnia and Herzegovina", content.weather.locationName)
            assertEquals(25, content.weather.current.temperatureCelsius)
        }

    @Test
    fun `permission denied refresh preserves content and opens city picker`() =
        runTest(mainDispatcherRule.dispatcher) {
            val deviceRepository = FakeDeviceLocationRepository()
            val viewModel = createViewModel(deviceLocationRepository = deviceRepository)
            viewModel.onLocationPermissionRequestResult(granted = true)
            advanceUntilIdle()
            deviceRepository.result = Result.Failure(AppFailure.PermissionDenied)

            viewModel.refresh()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
            assertTrue(viewModel.uiState.value.search.isVisible)
            assertTrue(viewModel.uiState.value.search.isLocationPermissionDenied)
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
        vararg locations: WeatherLocation,
    ) : DeviceLocationRepository {
        private val locations = locations.toList().ifEmpty { listOf(location()) }
        var result: Result<WeatherLocation>? = null
        var calls = 0
            private set

        override suspend fun getCurrentLocation(): Result<WeatherLocation> {
            val resolved = result ?: Result.Success(locations[calls.coerceAtMost(locations.lastIndex)])
            calls += 1
            return resolved
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

        fun forecast(
            location: WeatherLocation,
            temperature: Double = 25.0,
            currentTime: LocalDateTime = LocalDateTime.of(2026, 7, 31, 12, 0),
            dailyMaximumTemperature: Double = 26.0,
        ) = WeatherForecast(
            location = location,
            current =
                CurrentWeather(
                    time = currentTime,
                    temperature = temperature,
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
                        maxTemperature = dailyMaximumTemperature,
                        precipitationProbability = 10,
                    ),
                ),
        )
    }
}
