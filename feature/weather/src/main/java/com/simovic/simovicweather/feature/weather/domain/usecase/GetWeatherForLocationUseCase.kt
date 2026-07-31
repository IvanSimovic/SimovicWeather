package com.simovic.simovicweather.feature.weather.domain.usecase

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.model.WeatherLocation
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository

internal class GetWeatherForLocationUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(location: WeatherLocation): Result<WeatherForecast> = weatherRepository.getForecast(location)
}
