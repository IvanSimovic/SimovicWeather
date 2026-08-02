package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.location.DeviceLocationDataSource
import com.simovic.simovicweather.feature.weather.data.datasource.location.LocationName
import com.simovic.simovicweather.feature.weather.data.datasource.location.LocationNameDataSource
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DeviceLocationRepositoryImplTest {
    @Test
    fun `returns location with resolved name`() =
        runBlocking {
            val coordinates = Coordinates(43.85, 18.41)
            val repository =
                repository(
                    coordinates = { coordinates },
                    locationName = { LocationName("Sarajevo", "Sarajevo", "Bosnia and Herzegovina") },
                )

            val result = repository.getCurrentLocation()

            assertEquals(
                Result.Success(
                    WeatherLocation(
                        name = "Sarajevo",
                        region = "Sarajevo",
                        country = "Bosnia and Herzegovina",
                        coordinates = coordinates,
                    ),
                ),
                result,
            )
        }

    @Test
    fun `returns unavailable when coordinates cannot be resolved`() =
        runBlocking {
            var requestedName = false
            val repository =
                repository(
                    coordinates = { null },
                    locationName = {
                        requestedName = true
                        null
                    },
                )

            val result = repository.getCurrentLocation()

            assertEquals(Result.Failure(AppFailure.LocationUnavailable), result)
            assertFalse(requestedName)
        }

    @Test
    fun `continues without name when reverse geocoding fails`() =
        runBlocking {
            val coordinates = Coordinates(43.85, 18.41)
            val repository =
                repository(
                    coordinates = { coordinates },
                    locationName = { throw IllegalStateException("Geocoder unavailable") },
                )

            val result = repository.getCurrentLocation()

            assertEquals(
                Result.Success(WeatherLocation(name = "", coordinates = coordinates)),
                result,
            )
        }

    @Test
    fun `returns permission denied when location access throws security exception`() =
        runBlocking {
            val repository = repository(coordinates = { throw SecurityException("Permission denied") })

            val result = repository.getCurrentLocation()

            assertEquals(Result.Failure(AppFailure.PermissionDenied), result)
        }

    @Test
    fun `returns unknown failure when location access throws unexpected exception`() =
        runBlocking {
            val exception = IllegalStateException("Location service unavailable")
            val repository = repository(coordinates = { throw exception })

            val result = repository.getCurrentLocation() as Result.Failure

            assertSame(exception, (result.reason as AppFailure.Unknown).cause)
        }

    @Test
    fun `propagates cancellation from coordinate lookup`() {
        val cancellation = CancellationException("Location request cancelled")
        val repository = repository(coordinates = { throw cancellation })

        val thrown =
            assertThrows(CancellationException::class.java) {
                runBlocking { repository.getCurrentLocation() }
            }

        assertSame(cancellation, thrown)
    }

    @Test
    fun `propagates cancellation from reverse geocoding`() {
        val cancellation = CancellationException("Geocoding cancelled")
        val repository =
            repository(
                coordinates = { Coordinates(43.85, 18.41) },
                locationName = { throw cancellation },
            )

        val thrown =
            assertThrows(CancellationException::class.java) {
                runBlocking { repository.getCurrentLocation() }
            }

        assertSame(cancellation, thrown)
    }

    private fun repository(
        coordinates: suspend () -> Coordinates?,
        locationName: suspend (Coordinates) -> LocationName? = { null },
    ) = DeviceLocationRepositoryImpl(
        deviceLocationDataSource = DeviceLocationDataSource { coordinates() },
        locationNameDataSource = LocationNameDataSource(locationName),
    )
}
