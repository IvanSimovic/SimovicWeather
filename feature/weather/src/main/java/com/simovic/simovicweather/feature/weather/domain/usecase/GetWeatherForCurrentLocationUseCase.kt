package com.simovic.simovicweather.feature.weather.domain.usecase

import com.simovic.simovicweather.feature.base.domain.result.Result
import com.simovic.simovicweather.feature.weather.domain.model.WeatherForecast
import com.simovic.simovicweather.feature.weather.domain.repository.DeviceLocationRepository
import com.simovic.simovicweather.feature.weather.domain.repository.WeatherRepository

internal class GetWeatherForCurrentLocationUseCase(
    private val deviceLocationRepository: DeviceLocationRepository,
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(): Result<WeatherForecast> =
        when (val location = deviceLocationRepository.getCurrentLocation()) {
            is Result.Success -> weatherRepository.getForecast(location.value)
            is Result.Failure -> location
        }
}
