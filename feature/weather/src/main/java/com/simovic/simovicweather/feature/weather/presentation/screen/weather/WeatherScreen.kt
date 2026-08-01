package com.simovic.simovicweather.feature.weather.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simovic.simovicweather.feature.base.presentation.compose.composable.LoadingIndicator
import com.simovic.simovicweather.feature.base.presentation.compose.composable.WeatherBackground
import com.simovic.simovicweather.feature.base.presentation.ui.AppTheme
import com.simovic.simovicweather.feature.weather.R
import com.simovic.simovicweather.feature.weather.presentation.navigation.LocationSearchResult
import com.simovic.simovicweather.feature.weather.presentation.navigation.toWeatherLocation
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen(
    locationSearchResult: LocationSearchResult?,
    onConsumeLocationSearchResult: () -> Unit,
    onRequestLocationSearch: () -> Unit,
) {
    val viewModel = koinViewModel<WeatherViewModel>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var initialPermissionHandled by rememberSaveable { mutableStateOf(false) }
    val currentOnConsumeLocationSearchResult by rememberUpdatedState(onConsumeLocationSearchResult)
    val currentOnRequestLocationSearch by rememberUpdatedState(onRequestLocationSearch)

    LaunchedEffect(Unit) {
        if (!initialPermissionHandled) {
            initialPermissionHandled = true
            val isGranted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                viewModel.onCurrentLocationRequested()
            } else {
                currentOnRequestLocationSearch()
            }
        }
    }

    LaunchedEffect(locationSearchResult) {
        when (locationSearchResult) {
            null -> Unit
            LocationSearchResult.CurrentLocation -> viewModel.onCurrentLocationRequested()
            is LocationSearchResult.SelectedLocation ->
                viewModel.onLocationSelected(locationSearchResult.toWeatherLocation())
        }
        if (locationSearchResult != null) currentOnConsumeLocationSearchResult()
    }

    LaunchedEffect(uiState.shouldOpenLocationSearch) {
        if (uiState.shouldOpenLocationSearch) {
            currentOnRequestLocationSearch()
            viewModel.onLocationSearchRequestHandled()
        }
    }

    WeatherScreenContent(
        uiState = uiState,
        onUseCurrentLocation = onRequestLocationSearch,
        onRetry = viewModel::retry,
        onSearchOpen = onRequestLocationSearch,
        onRefresh = viewModel::refresh,
    )
}

@Composable
internal fun WeatherScreenContent(
    uiState: WeatherScreenUiState,
    onUseCurrentLocation: () -> Unit,
    onRetry: () -> Unit,
    onSearchOpen: () -> Unit,
    onRefresh: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    RefreshFeedbackEffect(
        refresh = uiState.refresh,
        snackbarHostState = snackbarHostState,
    )

    WeatherBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val forecast = uiState.forecast) {
                WeatherUiState.Initial ->
                    WeatherIntroductionContent(onUseCurrentLocation = onUseCurrentLocation)
                WeatherUiState.Loading -> LoadingIndicator()
                is WeatherUiState.Content ->
                    RefreshableWeatherContent(
                        weather = forecast.weather,
                        isRefreshing = uiState.refresh == RefreshUiState.Refreshing,
                        onRefresh = onRefresh,
                        onLocationClick = onSearchOpen,
                    )
                is WeatherUiState.Error ->
                    WeatherErrorContent(
                        reason = forecast.reason,
                        canRetry = forecast.canRetry,
                        onRetry = onRetry,
                    )
            }
            RefreshSnackbarHost(snackbarHostState)
        }
    }
}

@Composable
private fun BoxScope.RefreshSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(horizontal = AppTheme.dimensions.screenPadding),
    )
}

@Composable
private fun RefreshableWeatherContent(
    weather: WeatherUiModel,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLocationClick: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                containerColor = AppTheme.colors.card,
                color = AppTheme.colors.primary,
            )
        },
    ) {
        WeatherContent(weather = weather, onLocationClick = onLocationClick)
    }
}

@Composable
private fun RefreshFeedbackEffect(
    refresh: RefreshUiState,
    snackbarHostState: SnackbarHostState,
) {
    val message =
        when (refresh) {
            RefreshUiState.Idle, RefreshUiState.Refreshing -> return
            is RefreshUiState.Failed -> stringResource(refresh.reason.messageRes)
            RefreshUiState.UpToDate -> stringResource(R.string.weather_already_up_to_date)
        }
    LaunchedEffect(refresh) {
        snackbarHostState.showSnackbar(message)
    }
}
