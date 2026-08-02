package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simovic.simovicweather.feature.weather.R
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
        if (isGranted) {
            viewModel.onLocationPermissionResult(true)
        }
    }

    WeatherScreenContent(
        uiState = uiState,
        onUseCurrentLocation = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
    )
}

@Composable
private fun WeatherScreenContent(
    uiState: WeatherUiState,
    onUseCurrentLocation: () -> Unit,
) {
    if (uiState !is WeatherUiState.Initial && uiState !is WeatherUiState.PermissionRequired) return

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.location_permission_explanation))
        Button(onClick = onUseCurrentLocation) {
            Text(text = stringResource(R.string.use_current_location))
        }
    }
}
