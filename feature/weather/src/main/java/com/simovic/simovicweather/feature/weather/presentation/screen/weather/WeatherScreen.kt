package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    )
}

@Composable
internal fun WeatherScreenContent(
    uiState: WeatherUiState,
    onUseCurrentLocation: () -> Unit,
    onRetry: () -> Unit,
) {
    WeatherBackground {
        when (uiState) {
            is WeatherUiState.Initial ->
                WeatherIntroductionContent(onUseCurrentLocation = onUseCurrentLocation)
            is WeatherUiState.Loading -> LoadingIndicator()
            is WeatherUiState.Content -> WeatherContent(weather = uiState.weather)
            is WeatherUiState.PermissionRequired ->
                LocationPermissionContent(onUseCurrentLocation = onUseCurrentLocation)
            is WeatherUiState.Error ->
                WeatherErrorContent(
                    reason = uiState.reason,
                    canRetry = uiState.canRetry,
                    onRetry = onRetry,
                )
        }
    }
}
