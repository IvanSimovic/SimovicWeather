package com.simovic.simovicweather.feature.weather.presentation

import com.simovic.simovicweather.feature.weather.presentation.screen.weather.WeatherViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::WeatherViewModel)
    }
