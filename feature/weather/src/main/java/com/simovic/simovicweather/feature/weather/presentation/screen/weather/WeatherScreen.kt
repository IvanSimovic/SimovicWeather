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
import androidx.compose.runtime.remember
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen() {
    val viewModel = koinViewModel<WeatherViewModel>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onLocationPermissionRequestResult(granted)
        }

    LaunchedEffect(Unit) {
        val isGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        viewModel.onInitialLocationPermissionChecked(isGranted)
    }

    WeatherScreenContent(
        uiState = uiState,
        onUseCurrentLocation = {
            val isGranted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                viewModel.onCurrentLocationRequested()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
        onRetry = viewModel::retry,
        onSearchOpen = viewModel::onSearchOpened,
        onSearchClose = viewModel::onSearchClosed,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onSearchClear = { viewModel.onSearchQueryChanged("") },
        onSearchRetry = viewModel::retrySearch,
        onLocationSelect = viewModel::onLocationSelected,
        onRefresh = viewModel::refresh,
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
    onRefresh: () -> Unit,
) {
    val searchVisibilityState = remember { MutableTransitionState(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    searchVisibilityState.targetState = uiState.search.isVisible
    RefreshFeedbackEffect(
        refresh = uiState.refresh,
        snackbarHostState = snackbarHostState,
    )

    BackHandler(enabled = uiState.search.isVisible, onBack = onSearchClose)
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
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
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
            RefreshUiState.Idle,
            RefreshUiState.Refreshing,
            -> return
            is RefreshUiState.Failed -> stringResource(refresh.reason.messageRes)
            RefreshUiState.UpToDate -> stringResource(R.string.weather_already_up_to_date)
        }
    LaunchedEffect(refresh) {
        snackbarHostState.showSnackbar(message)
    }
}
