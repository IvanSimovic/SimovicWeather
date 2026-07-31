package com.simovic.simovicweather.feature.weather.domain.usecase

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.CurrentWeather
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GetWeatherForCurrentLocationUseCaseTest {
    @Test
    fun `loads forecast for current location`() =
        runBlocking {
            val location = weatherLocation()
            val forecast = weatherForecast(location)
            val weatherRepository = FakeWeatherRepository(Result.Success(forecast))
            val useCase =
                GetWeatherForCurrentLocationUseCase(
                    deviceLocationRepository = FakeDeviceLocationRepository(Result.Success(location)),
                    weatherRepository = weatherRepository,
                )

            val result = useCase()

            assertEquals(Result.Success(forecast), result)
            assertEquals(listOf(location), weatherRepository.requestedLocations)
        }

    @Test
    fun `does not load forecast when current location is unavailable`() =
        runBlocking {
            val failure = Result.Failure(AppFailure.LocationUnavailable)
            val weatherRepository = FakeWeatherRepository(Result.Failure(AppFailure.Unknown()))
            val useCase =
                GetWeatherForCurrentLocationUseCase(
                    deviceLocationRepository = FakeDeviceLocationRepository(failure),
                    weatherRepository = weatherRepository,
                )

            val result = useCase()

            assertEquals(failure, result)
            assertEquals(emptyList<WeatherLocation>(), weatherRepository.requestedLocations)
        }

    private class FakeDeviceLocationRepository(
        private val result: Result<WeatherLocation>,
    ) : DeviceLocationRepository {
        override suspend fun getCurrentLocation(): Result<WeatherLocation> = result
    }

    private class FakeWeatherRepository(
        private val result: Result<WeatherForecast>,
    ) : WeatherRepository {
        val requestedLocations = mutableListOf<WeatherLocation>()

        override suspend fun getForecast(location: WeatherLocation): Result<WeatherForecast> {
            requestedLocations += location
            return result
        }
    }

    private companion object {
        fun weatherLocation() =
            WeatherLocation(
                id = 1,
                name = "Sarajevo",
                coordinates = Coordinates(43.85, 18.41),
            )

        fun weatherForecast(location: WeatherLocation) =
            WeatherForecast(
                location = location,
                current =
                    CurrentWeather(
                        time = LocalDateTime.of(2026, 8, 3, 12, 0),
                        temperature = 28.0,
                        apparentTemperature = 29.0,
                        humidity = 40,
                        precipitation = 0.0,
                        weatherCode = 0,
                        pressure = 1015.0,
                        windSpeed = 8.0,
                    ),
                days = emptyList(),
            )
    }
}
