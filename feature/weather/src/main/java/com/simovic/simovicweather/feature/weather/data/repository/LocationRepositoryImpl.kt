package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.GeocodingRetrofitService
import com.simovic.simovicweather.feature.weather.data.mapper.LocationMapper
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.LocationRepository

internal class LocationRepositoryImpl(
    private val service: GeocodingRetrofitService,
    private val mapper: LocationMapper,
) : LocationRepository {
    override suspend fun search(
        query: String,
        language: String,
    ): Result<List<WeatherLocation>> =
        service.search(query, language = language).toDomain { response ->
            Result.Success(response.results.map(mapper::toDomain))
        }
}
