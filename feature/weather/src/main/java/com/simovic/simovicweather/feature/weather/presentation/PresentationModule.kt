package com.simovic.simovicweather.feature.weather.presentation

import com.simovic.simovicweather.feature.weather.presentation.screen.weather.WeatherDateFormatter
import com.simovic.simovicweather.feature.weather.presentation.screen.weather.WeatherPresentationMapper
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        single { WeatherDateFormatter() }
        singleOf(::WeatherPresentationMapper)
    }
