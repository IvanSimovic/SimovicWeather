package com.simovic.simovicweather.presentation

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simovic.simovicweather.feature.base.presentation.navigation.NavigationScreens
import com.simovic.simovicweather.feature.weather.presentation.navigation.LocationSearchResult
import com.simovic.simovicweather.feature.weather.presentation.screen.weather.LocationSearchScreen
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
        composable<NavigationScreens.Weather>(
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
        ) { backStackEntry ->
            val locationSearchResult by
                backStackEntry.savedStateHandle
                    .getStateFlow<LocationSearchResult?>(LOCATION_SEARCH_RESULT_KEY, null)
                    .collectAsStateWithLifecycle()
            WeatherScreen(
                locationSearchResult = locationSearchResult,
                onConsumeLocationSearchResult = {
                    backStackEntry.savedStateHandle.remove<LocationSearchResult>(LOCATION_SEARCH_RESULT_KEY)
                },
                onRequestLocationSearch = {
                    navController.navigate(NavigationScreens.LocationSearch) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<NavigationScreens.LocationSearch>(
            enterTransition = {
                slideInVertically(
                    animationSpec = LocationSearchTransitionSpec,
                    initialOffsetY = { fullHeight -> -fullHeight },
                )
            },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = LocationSearchTransitionSpec,
                    targetOffsetY = { fullHeight -> -fullHeight },
                )
            },
        ) {
            LocationSearchScreen(
                shouldRequestFocus =
                    transition.currentState == EnterExitState.Visible &&
                        transition.targetState == EnterExitState.Visible,
                onClose = navController::popBackStack,
                onResult = { result ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(LOCATION_SEARCH_RESULT_KEY, result)
                    navController.popBackStack()
                },
            )
        }
    }
}

private const val LOCATION_SEARCH_RESULT_KEY = "location_search_result"
private const val LOCATION_SEARCH_TRANSITION_DURATION_MILLIS = 400

private val LocationSearchTransitionSpec =
    tween<IntOffset>(
        durationMillis = LOCATION_SEARCH_TRANSITION_DURATION_MILLIS,
        easing = FastOutSlowInEasing,
    )
