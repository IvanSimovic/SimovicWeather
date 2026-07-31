package com.simovic.simovicweather.feature.weather.domain.usecase

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository

internal class SearchLocationsUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(
        query: String,
        language: String,
    ): Result<List<WeatherLocation>> {
        val normalized = query.trim()
        if (normalized.length < MINIMUM_QUERY_LENGTH) return Result.Success(emptyList())

        return when (val result = locationRepository.search(normalized, language)) {
            is Result.Success ->
                Result.Success(
                    result.value.distinctBy { location ->
                        location.id ?: "${location.coordinates.latitude},${location.coordinates.longitude}"
                    },
                )
            is Result.Failure -> result
        }
    }

    private companion object {
        const val MINIMUM_QUERY_LENGTH = 3
    }
}
