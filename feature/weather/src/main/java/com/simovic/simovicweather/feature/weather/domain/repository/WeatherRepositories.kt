package com.simovic.simovicweather.feature.weather.domain.repository

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation

internal interface WeatherRepository {
    suspend fun getForecast(location: WeatherLocation): Result<WeatherForecast>
}

internal interface LocationRepository {
    suspend fun search(
        query: String,
        language: String,
    ): Result<List<WeatherLocation>>
}

internal interface DeviceLocationRepository {
    suspend fun getCurrentLocation(): Result<WeatherLocation>
}
