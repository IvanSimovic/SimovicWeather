package com.simovic.simovicweather.feature.weather.domain.usecase

import com.simovic.simovicweather.feature.base.domain.result.AppFailure
import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.Coordinates
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchLocationsUseCaseTest {
    @Test
    fun `query shorter than three characters does not call repository`() =
        runBlocking {
            val repository = FakeLocationRepository()

            val result = SearchLocationsUseCase(repository)("ab", "en")

            assertEquals(0, repository.calls)
            assertEquals(emptyList<WeatherLocation>(), (result as Result.Success).value)
        }

    @Test
    fun `locations with the same id are deduplicated`() =
        runBlocking {
            val location = WeatherLocation(1, "Sarajevo", coordinates = Coordinates(43.85, 18.41))
            val repository = FakeLocationRepository(Result.Success(listOf(location, location)))

            val result = SearchLocationsUseCase(repository)(" Sarajevo ", "en")

            assertEquals(1, repository.calls)
            assertEquals("Sarajevo", repository.query)
            assertEquals("en", repository.language)
            assertEquals(listOf(location), (result as Result.Success).value)
        }

    @Test
    fun `locations without ids are deduplicated by coordinates`() =
        runBlocking {
            val coordinates = Coordinates(43.85, 18.41)
            val first = WeatherLocation(name = "Sarajevo", coordinates = coordinates)
            val duplicate = WeatherLocation(name = "Sarajevo City", coordinates = coordinates)
            val repository = FakeLocationRepository(Result.Success(listOf(first, duplicate)))

            val result = SearchLocationsUseCase(repository)("Sarajevo", "en")

            assertEquals(listOf(first), (result as Result.Success).value)
        }

    @Test
    fun `repository failure is returned`() =
        runBlocking {
            val failure = Result.Failure(AppFailure.Connectivity())
            val repository = FakeLocationRepository(failure)

            val result = SearchLocationsUseCase(repository)("Sarajevo", "en")

            assertEquals(failure, result)
        }

    private class FakeLocationRepository(
        private val result: Result<List<WeatherLocation>> = Result.Success(emptyList()),
    ) : LocationRepository {
        var calls = 0
        var query: String? = null
        var language: String? = null

        override suspend fun search(
            query: String,
            language: String,
        ): Result<List<WeatherLocation>> {
            calls += 1
            this.query = query
            this.language = language
            return result
        }
    }
}
