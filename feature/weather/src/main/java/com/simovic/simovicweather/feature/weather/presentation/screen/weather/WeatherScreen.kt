package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen() {
    koinViewModel<WeatherViewModel>()
}
