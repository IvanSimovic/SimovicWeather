package com.simovic.simovicweather.feature.weather.domain

import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForCurrentLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.GetWeatherForLocationUseCase
import com.simovic.simovicweather.feature.weather.domain.usecase.SearchLocationsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetWeatherForCurrentLocationUseCase)
        singleOf(::GetWeatherForLocationUseCase)
        singleOf(::SearchLocationsUseCase)
    }
