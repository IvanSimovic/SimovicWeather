package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.DailyWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForCurrentLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForLocationUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
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
    fun `selecting location loads selected forecast`() =
        runTest(mainDispatcherRule.dispatcher) {
            val weatherRepository = FakeWeatherRepository()
            val selectedLocation = location(id = 2, name = "Mostar")
            val viewModel = createViewModel(weatherRepository = weatherRepository)

            viewModel.onLocationSelected(selectedLocation)
            advanceUntilIdle()

            val target = viewModel.uiState.value.target as ForecastTarget.SelectedLocation
            assertEquals(selectedLocation, target.location.location)
            assertEquals(listOf(selectedLocation), weatherRepository.locations)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
        }

    @Test
    fun `current location request loads current location`() =
        runTest(mainDispatcherRule.dispatcher) {
            val currentLocation = location(id = 3, name = "Current")
            val weatherRepository = FakeWeatherRepository()
            val viewModel =
                createViewModel(
                    weatherRepository = weatherRepository,
                    deviceLocationRepository = FakeDeviceLocationRepository(currentLocation),
                )
            viewModel.onCurrentLocationRequested()
            advanceUntilIdle()

            assertEquals(ForecastTarget.CurrentLocation, viewModel.uiState.value.target)
            assertEquals(listOf(currentLocation), weatherRepository.locations)
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
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

            viewModel.onLocationSelected(firstLocation)
            runCurrent()
            viewModel.onLocationSelected(secondLocation)
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
            viewModel.onLocationSelected(selectedLocation)
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

            viewModel.onCurrentLocationRequested()
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(location())
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(selectedLocation)
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
            viewModel.onLocationSelected(firstLocation)
            advanceUntilIdle()

            viewModel.refresh()
            runCurrent()
            viewModel.onLocationSelected(secondLocation)
            runCurrent()
            refreshResult.complete(Result.Success(forecast(firstLocation, temperature = 30.0)))
            advanceUntilIdle()

            val content = viewModel.uiState.value.forecast as WeatherUiState.Content
            assertEquals("Mostar, Bosnia and Herzegovina", content.weather.locationName)
            assertEquals(25, content.weather.current.temperatureCelsius)
        }

    @Test
    fun `permission denied refresh preserves content and requests location search`() =
        runTest(mainDispatcherRule.dispatcher) {
            val deviceRepository = FakeDeviceLocationRepository()
            val viewModel = createViewModel(deviceLocationRepository = deviceRepository)
            viewModel.onCurrentLocationRequested()
            advanceUntilIdle()
            deviceRepository.result = Result.Failure(AppFailure.PermissionDenied)

            viewModel.refresh()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
            assertEquals(RefreshUiState.Idle, viewModel.uiState.value.refresh)
            assertTrue(viewModel.uiState.value.shouldOpenLocationSearch)
        }

    @Test
    fun `permission denied full load preserves forecast and requests location search`() =
        runTest(mainDispatcherRule.dispatcher) {
            val deviceRepository = FakeDeviceLocationRepository()
            val viewModel = createViewModel(deviceLocationRepository = deviceRepository)
            viewModel.onLocationSelected(location(name = "Mostar"))
            advanceUntilIdle()
            val previousForecast = viewModel.uiState.value.forecast
            deviceRepository.result = Result.Failure(AppFailure.PermissionDenied)

            viewModel.onCurrentLocationRequested()
            advanceUntilIdle()

            assertEquals(previousForecast, viewModel.uiState.value.forecast)
            assertTrue(viewModel.uiState.value.shouldOpenLocationSearch)
        }

    @Test
    fun `full screen retry reloads the selected target`() =
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

            viewModel.onLocationSelected(selectedLocation)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Error)

            viewModel.retry()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.forecast is WeatherUiState.Content)
            assertEquals(listOf(selectedLocation, selectedLocation), weatherRepository.locations)
        }

    private fun createViewModel(
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository(),
        deviceLocationRepository: FakeDeviceLocationRepository = FakeDeviceLocationRepository(),
    ) = WeatherViewModel(
        getCurrentWeather =
            GetWeatherForCurrentLocationUseCase(
                deviceLocationRepository = deviceLocationRepository,
                weatherRepository = weatherRepository,
            ),
        getWeatherForLocation = GetWeatherForLocationUseCase(weatherRepository),
        presentationMapper =
            WeatherPresentationMapper(
                WeatherDateFormatter(
                    LocalizedDatePatternProvider { _, _ -> "EEE, MMM d" },
                    localeProvider = { Locale.US },
                ),
            ),
    )

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
