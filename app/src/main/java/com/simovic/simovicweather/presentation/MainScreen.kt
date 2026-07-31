package com.simovic.simovicweather.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simovic.simovicweather.feature.base.presentation.navigation.NavigationScreens
import com.simovic.simovicweather.feature.weather.presentation.screen.weather.WeatherScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    MainNavHost(navController)
}

@Composable
private fun MainNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreens.Weather,
    ) {
        composable<NavigationScreens.Weather> {
            WeatherScreen()
        }
    }
}
