package com.simovic.simovicweather.feature.weather.data.repository

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.data.datasource.api.service.WeatherRetrofitService
import com.simovic.simovicweather.feature.weather.data.mapper.WeatherMapper
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository

internal class WeatherRepositoryImpl(
    private val service: WeatherRetrofitService,
    private val mapper: WeatherMapper,
) : WeatherRepository {
    override suspend fun getForecast(location: WeatherLocation): Result<WeatherForecast> =
        service
            .getForecast(
                latitude = location.coordinates.latitude,
                longitude = location.coordinates.longitude,
            ).toDomain { mapper.toDomain(it, location) }
}
