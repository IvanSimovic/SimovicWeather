package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simovic.simovicweather.feature.base.presentation.compose.composable.LoadingIndicator
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherBackground
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen() {
    val viewModel = koinViewModel<WeatherViewModel>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onLocationPermissionResult(granted)
        }

    LaunchedEffect(Unit) {
        val isGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (isGranted) viewModel.onLocationPermissionResult(true)
    }

    WeatherScreenContent(
        uiState = uiState,
        onUseCurrentLocation = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onRetry = viewModel::retry,
        onSearchOpen = viewModel::onSearchOpened,
        onSearchClose = viewModel::onSearchClosed,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onSearchClear = viewModel::onSearchCleared,
        onSearchRetry = viewModel::retrySearch,
        onLocationSelect = viewModel::onLocationSelected,
    )
}

@Composable
internal fun WeatherScreenContent(
    uiState: WeatherScreenUiState,
    onUseCurrentLocation: () -> Unit,
    onRetry: () -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onSearchRetry: () -> Unit,
    onLocationSelect: (LocationUiModel) -> Unit,
) {
    val searchVisibilityState = remember { MutableTransitionState(false) }
    searchVisibilityState.targetState = uiState.search.isVisible

    BackHandler(enabled = uiState.search.isVisible, onBack = onSearchClose)
    WeatherBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val forecast = uiState.forecast) {
                WeatherUiState.Initial ->
                    WeatherIntroductionContent(onUseCurrentLocation = onUseCurrentLocation)
                WeatherUiState.Loading -> LoadingIndicator()
                is WeatherUiState.Content ->
                    WeatherContent(
                        weather = forecast.weather,
                        onLocationClick = onSearchOpen,
                    )
                WeatherUiState.PermissionRequired ->
                    LocationPermissionContent(onUseCurrentLocation = onUseCurrentLocation)
                is WeatherUiState.Error ->
                    WeatherErrorContent(
                        reason = forecast.reason,
                        canRetry = forecast.canRetry,
                        onRetry = onRetry,
                    )
            }
            AnimatedVisibility(
                visibleState = searchVisibilityState,
                enter =
                    expandVertically(
                        animationSpec = tween(),
                        expandFrom = Alignment.Top,
                    ),
                exit =
                    shrinkVertically(
                        animationSpec = tween(),
                        shrinkTowards = Alignment.Top,
                    ),
            ) {
                CitySearchOverlay(
                    search = uiState.search,
                    shouldRequestFocus =
                        searchVisibilityState.currentState && searchVisibilityState.isIdle,
                    onQueryChange = onSearchQueryChange,
                    onSearchClear = onSearchClear,
                    onSearchClose = onSearchClose,
                    onRetry = onSearchRetry,
                    onLocationSelect = onLocationSelect,
                    onUseCurrentLocation = onUseCurrentLocation,
                )
            }
        }
    }
}
