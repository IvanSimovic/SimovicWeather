package com.simovic.simovicweather.feature.weather

import com.simovic.simovicweather.feature.weather.data.dataModule
import com.simovic.simovicweather.feature.weather.domain.domainModule
import com.simovic.simovicweather.feature.weather.presentation.presentationModule

val featureWeatherModules = listOf(presentationModule, domainModule, dataModule)
